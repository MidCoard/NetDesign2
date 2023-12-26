package top.focess.netdesign.server

import kotlinx.coroutines.*
import top.focess.netdesign.ai.ChatGPTAccessor
import top.focess.netdesign.ai.ChatGPTModel
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.GlobalState.contacts
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.server.toMessage
import top.focess.netdesign.sqldelight.contact.Contact
import top.focess.netdesign.sqldelight.message.ServerMessage
import top.focess.netdesign.ui.*
import top.focess.netdesign.ui.sha256
import top.focess.scheduler.FocessScheduler
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap

@OptIn(DelicateCoroutinesApi::class)
class LocalServer(val name: String, port: Int = NetworkConfig.DEFAULT_SERVER_PORT, chatgptKey: String? = null) : Closeable {

    private val clientScopeMap = mutableMapOf<String, ClientScope>()

    private val challengeMap = mutableMapOf<String, String>()

    private val defaultContact = Friend(0, name, true)

    private val serverSocket = ServerSocket(port)

    private var shouldClose = false

    private val scheduler: FocessScheduler = FocessScheduler("SingleServer", true)

    private val packetQueue = ConcurrentLinkedQueue<Pair<ClientScope, ServerPacket>>()

    private val chatGPTAccessor = chatgptKey?.let { ChatGPTAccessor(it, ChatGPTModel.GPT4_TURBO) }

    private val delayMessageList = ConcurrentHashMap<String, Pair<List<Int>, Message>>()

    init {
        contacts.add(defaultContact)
        contacts.addAll(contactQueries.selectAll().executeAsList().map { it.toContact() })

        contacts.forEach {
            if (it.id != 0) {
                it.messages.addAll(queryMessages(0, it.id))
            }
        }
    }

    // make sure all packets are sent in one thread
    // in fact, different clients' packets can be sent in different threads
    // but one client's packets must be sent in one thread
    private val packetThread = Thread {
        while (!this.shouldClose) {
            // give up the cpu
            runBlocking { delay(100) }
            packetQueue.poll()?.let {
                val clientScope = it.first
                val serverPacket = it.second
                runBlocking {
                    sendChannelPacket0(clientScope, serverPacket)
                }
            }
        }
    }

    init {
        // 10 seconds to send a heart packet
        scheduler.runTimer(Runnable {
            clientScopeMap.forEach { (_, clientScope) -> sendChannelPacket(clientScope, ChannelHeartRequestPacket()) }
        }, Duration.ZERO, Duration.ofSeconds(10))

        Thread {
            while (!this.shouldClose) {
                try {
                    val socket = serverSocket.accept()

                    GlobalScope.launch {

                        try {
                            BufferedInputStream(socket.getInputStream()).let {
                                withTimeout(3000) {
                                    val bytes = it.readAvailableBytes()
                                    val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                                    val clientPacket = Packets.fromProtoPacket(protoPacket) as ClientPacket
                                    println("SingleServer: receive $clientPacket")
                                    with(DEFAULT_PACKET_HANDLER) {
                                        this@LocalServer.handle(Packets.fromProtoPacket(protoPacket) as ClientPacket)
                                    }?.to(clientPacket)
                                }
                            }?.let {
                                val serverPacket = it.first
                                println("SingleServer: send $serverPacket");
                                val bytes = serverPacket.toProtoPacket().toByteArray()
                                println("send ${bytes.size} bytes")
                                BufferedOutputStream(socket.getOutputStream()).let {
                                    it.write(bytes)
                                    it.flush()
                                }
                                val clientPacket = it.second
                                if (clientPacket is SetupChannelRequestPacket)
                                    setupChannel(socket, clientPacket.token)
                                else {
                                    socket.shutdownOutput()
                                    socket.close()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (e is TimeoutCancellationException)
                                println("SingleServer: receive timeout")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()

        packetThread.start()
    }

    override fun close() {
        shouldClose = true;
        packetThread.join()
        serverSocket.close()
    }


    private fun setupChannel(socket: Socket, token: String) {
        // re setup channel is accepted because the channel will be closed if some error occurs
        val clientScope = this.clientScopeMap[token] ?: return
        clientScope.channelSocket = socket
        clientScope.isChannelSetup = true
        GlobalScope.launch {
            delay(200)
            val info = contactList(clientScope)
            sendChannelPacket(clientScope, ContactListRequestPacket(info.first, info.second))
        }
    }

    private fun contactList(clientScope: ClientScope): Pair<List<Friend>, List<Int>> {
        if (chatGPTAccessor != null)
            return Pair(
                listOf(defaultContact, clientScope.self, Friend(chatGPTAccessor.id, "ChatGPT", true)),
                listOf(queryNewestInternalId(defaultContact.id.toLong(), clientScope.self.id.toLong()),
                    0,
                    queryNewestInternalId(chatGPTAccessor.id.toLong(), clientScope.self.id.toLong()))
            )
        return Pair(
            listOf(defaultContact, clientScope.self),
            listOf(queryNewestInternalId(defaultContact.id.toLong(), clientScope.self.id.toLong()),
            0))
    }

    companion object {

        val DEFAULT_PACKET_HANDLER = object : SingleServerPacketHandler {

            override fun LocalServer.handle(packet: ClientPacket): ServerPacket? {
                return when (packet) {
                    is ServerStatusRequestPacket -> {
                        ServerStatusResponsePacket(
                            online = false,
                            registrable = true
                        )
                    }

                    is RegisterRequestPacket -> {
                        if (packet.username.length in 6..20) {
                            val friend = friendQueries.selectByName(packet.username).executeAsOneOrNull()
                            if (friend == null) {
                                contactQueries.insert(packet.username, 0)
                                val contact = contactQueries.selectByName(packet.username).executeAsOneOrNull()
                                if (contact != null) {
                                    friendQueries.insert(contact.id, packet.username, packet.rawPassword.sha256())
                                    contacts.add(ServerFriend(contact.id.toInt(), contact.name))
                                    return RegisterResponsePacket(true)
                                }
                            }
                        }
                        RegisterResponsePacket(false)
                    }

                    is LoginPreRequestPacket -> {
                        val challenge = genChallenge()
                        this.challengeMap[packet.username] = challenge
                        LoginPreResponsePacket(
                            challenge
                        )
                    }

                    is LoginRequestPacket -> {
                        if (packet.username.length in 6..20) {
                            val challenge = challengeMap[packet.username]
                            if (challenge != null) {
                                val friend = friendQueries.selectByName(packet.username).executeAsOneOrNull()
                                if (friend != null) {
                                    val hashPassword = (friend.password + challenge).sha256()
                                    if (hashPassword == packet.hashPassword) {
                                        val token = genToken()
                                        val clientScope = ClientScope(packet.username, token, friend.id.toInt())
                                        this.clientScopeMap[token] = clientScope
                                        val serverFriend = contacts.find { it.id == friend.id.toInt() } as ServerFriend
                                        serverFriend.clientScope = clientScope
                                        return LoginResponsePacket(true, friend.id.toInt(), token)
                                    }
                                }
                            }
                        }
                        LoginResponsePacket(false, -1, "")
                    }

                    is ServerStatusUpdateRequestPacket ->
                        ServerStatusUpdateResponsePacket(
                            online = false,
                            registrable = true
                        )

                    is SetupChannelRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null)
                            if (!clientScope.isChannelSetup)
                                return ServerAckResponsePacket()
                        // if the channel is already setup, return null to reject the request
                        null
                    }
                    is ContactMessageRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null) {
                            if (packet.id == 0)
                                return ContactMessageResponsePacket(
                                    queryMessage(0, clientScope.self.id, packet.internalId) ?: EMPTY_MESSAGE
                                )
                            else if (chatGPTAccessor != null && chatGPTAccessor.id == packet.id)
                                return ContactMessageResponsePacket(
                                    queryMessage(packet.id, clientScope.self.id, packet.internalId) ?: EMPTY_MESSAGE
                                )
                        }
                        ContactMessageResponsePacket(EMPTY_MESSAGE)
                    }

                    is SendMessageRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null)
                            if (clientScope.id == packet.from && packet.messageContent.content.length < 1000)
                                if (packet.from == 0 || packet.to == 0 || (chatGPTAccessor != null && chatGPTAccessor.id == packet.to)) {
                                    var content = packet.messageContent.content

                                    if (packet.messageContent.type == MessageType.FILE || packet.messageContent.type == MessageType.IMAGE) {
                                        content = UUID.randomUUID().toString()
                                        fileQueries.insertFile(content, clientScope.id.toLong())
                                        fileQueries.insertFile(content, packet.to.toLong())
                                    }

                                    val insertedMessage = insertMessage(
                                        packet.from,
                                        packet.to,
                                        content,
                                        packet.messageContent.type
                                    )
                                    var chatgpt = false
                                    chatGPTAccessor?.let {
                                        if (chatGPTAccessor.id == packet.to) {
                                            with(chatGPTAccessor) {
                                                sendMessage(clientScope.id, clientScope.username, packet.messageContent)
                                                chatgpt = true
                                            }
                                        }
                                    }
                                    if (!chatgpt)
                                        if (insertedMessage.content is TextMessageContent)
                                            sendChannelPacket(packet.to, ContactMessageListRequestPacket(insertedMessage))
                                        else
                                            delayMessageList[content] = listOf(packet.to) to insertedMessage
                                    return SendMessageResponsePacket(
                                        insertedMessage
                                    )
                                } else {
                                    val target = contactQueries.selectById(packet.to.toLong()).executeAsOneOrNull()
                                    if (target != null) {
                                        val targetContact = try {
                                            target.toContact()
                                        } catch (e: Exception) {null}
                                        if (targetContact is Group) {
                                            if (targetContact.members.find { it.id == packet.from } != null) {
                                                var content = packet.messageContent.content

                                                if (packet.messageContent.type == MessageType.FILE || packet.messageContent.type == MessageType.IMAGE) {
                                                    content = UUID.randomUUID().toString()
                                                    fileQueries.insertFile(content, clientScope.id.toLong())
                                                    targetContact.members.forEach{
                                                        if (it.id != 0)
                                                            fileQueries.insertFile(content, it.id.toLong())
                                                    }
                                                }

                                                val insertedMessage = insertMessage(
                                                    packet.from,
                                                    packet.to,
                                                    content,
                                                    packet.messageContent.type
                                                )
                                                if (insertedMessage.content is TextMessageContent)
                                                    targetContact.members.forEach {
                                                        if (it.id != packet.from)
                                                            sendChannelPacket(
                                                                it.id,
                                                                ContactMessageListRequestPacket(insertedMessage)
                                                            )
                                                    }
                                                else
                                                    delayMessageList[content] = targetContact.members.map{it.id}.filter {it != 0} to insertedMessage
                                                return SendMessageResponsePacket(
                                                    insertedMessage
                                                )
                                            }
                                        }
                                    }
                                }
                        SendMessageResponsePacket(EMPTY_MESSAGE)
                    }

                    is DeleteMessageRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null) {
                            val message = serverMessageQueries.selectPreciseById(packet.id.toLong()).executeAsOneOrNull()
                            if (message != null)
                                if (message.sender.toInt() == clientScope.id && message.type == MessageType.FILE)
                                    if (fileQueries.selectFileData(message.data_).executeAsOneOrNull() == null) {
                                        serverMessageQueries.deleteById(packet.id.toLong())
                                        return DeleteMessageResponsePacket(true)
                                    }
                        }
                        DeleteMessageResponsePacket(false)
                    }

                    is FileUploadRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null && packet.file.filename.isNotEmpty()) {
                            val file = fileQueries.selectFilePrecise(packet.id, clientScope.id.toLong()).executeAsOneOrNull()
                            if (file != null) {
                                val fileData = fileQueries.selectFileData(file.fileId).executeAsOneOrNull();
                                if (fileData == null && packet.file.data.sha256() == packet.hash) {
                                    fileQueries.insertFileData(file.fileId, packet.file.filename, packet.file.data, packet.hash)
                                    val pair = delayMessageList[packet.id]
                                    pair?.first?.forEach {
                                        sendChannelPacket(
                                            it,
                                            ContactMessageListRequestPacket(pair.second)
                                        )
                                    }
                                    return FileUploadResponsePacket(true)
                                }
                            }
                        }
                        FileUploadResponsePacket(false)
                    }

                    is FileDownloadRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null) {
                            val file = fileQueries.selectFilePrecise(packet.id, clientScope.id.toLong()).executeAsOneOrNull()
                            if (file != null) {
                                val fileData = fileQueries.selectFileData(file.fileId).executeAsOneOrNull();
                                if (fileData != null)
                                    return FileDownloadResponsePacket(File(fileData.filename, fileData.data_), fileData.hash)
                            }
                        }
                        FileDownloadResponsePacket(EMPTY_FILE, "")
                    }

                    else -> throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
                }

            }
        }
    }


    data class ClientScope(
        val username: String,
        val token: String,
        val id: Int,
        val self: Friend = Friend(id, username, true)
    ) {
        lateinit var channelSocket: Socket
        var isChannelSetup = false
    }

    interface SingleServerPacketHandler {
        fun LocalServer.handle(packet: ClientPacket): ServerPacket?
    }

    private suspend fun sendChannelPacket0(clientScope: ClientScope, packet: ServerPacket) {
        if (!clientScope.isChannelSetup) return
        if (clientScope.channelSocket.isClosed) {
            // channel is closed by os
            clientScope.isChannelSetup = false
            return
        }
        println("SingleServerChannel: client ${clientScope.id} send ${packet.packetId}")
        clientScope.channelSocket.let {
            BufferedOutputStream(it.getOutputStream()).let {
                it.write(packet.toProtoPacket().toByteArray())
                it.flush()
            }

            try {
                // call and wait for 3000ms
                withTimeout(3000) {
                    val bytes = it.getInputStream().readAvailableBytes()
                    val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                    val clientPacket = Packets.fromProtoPacket(protoPacket) as ClientPacket
                    if (clientPacket !is ClientAckResponsePacket)
                        throw IllegalStateException("Client did not ack packet")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is TimeoutCancellationException)
                    println("SingleServerChannel: receive timeout")
                clientScope.isChannelSetup = false
                clientScope.channelSocket.close()
            }
        }
    }

    private fun sendChannelPacket(clientScope: ClientScope, packet: ServerPacket) {
        this.packetQueue.add(clientScope to packet)
    }

    fun sendChannelPacket(id: Int, packet: ServerPacket) {
        println("server should send to $id with packet: $packet")
        if (id != 0)
            sendChannelPacket(this.clientScopeMap.values.find { it.id == id } ?: return, packet)
        else {
            when(packet) {
                is ContactMessageListRequestPacket -> {
                    packet.messages.forEach {
                        getContact(it.from)?.messages?.add(it)
                    }
                }
            }
        }
    }

    fun getClient(): Client {
        val token = genToken()
        this.clientScopeMap[token] = ClientScope(name, token,0, defaultContact)
        return LocalClient(false, true, defaultContact, token)
    }


}

private fun genChallenge(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun genToken() = genChallenge()

private fun queryMessage(a: Int, b: Int, internalId: Int): Message? {
    serverMessageQueries.selectPrecise(a.toLong(), b.toLong(), internalId.toLong()).executeAsOneOrNull()?.toMessage()
        ?.let {
            return it
        }

    serverMessageQueries.selectPrecise(b.toLong(), a.toLong(), internalId.toLong()).executeAsOneOrNull()?.toMessage()
        ?.let {
            return it
        }

    return null
}

private fun queryMessages(a: Int, b: Int) : List<Message> {
    val messages = serverMessageQueries.selectLatest(a.toLong(), b.toLong()).executeAsList().map { it.toMessage() }.toMutableList()
    messages.addAll(serverMessageQueries.selectLatest(b.toLong(), a.toLong()).executeAsList().map { it.toMessage() })
    return messages
}


internal fun ServerMessage.toMessage() = Message(
    id.toInt(),
    sender.toInt(),
    receiver_.toInt(),
    internal_id.toInt(),
    when (type) {
        MessageType.TEXT -> TextMessageContent(data_)
        MessageType.IMAGE -> ImageMessageContent(data_)
        MessageType.FILE -> FileMessageContent(data_)
    },
    timestamp.toInt()
)


val LOCK = Any()

private fun queryNewestMessage(a: Long, b: Long): Message? {
    val message = serverMessageQueries.selectNewest(a, b).executeAsOneOrNull()?.toMessage()

    val message2 = serverMessageQueries.selectNewest(b, a).executeAsOneOrNull()?.toMessage()

    if (message != null && message2 != null)
        return if (message.internalId > message2.internalId) message else message2

    return message ?: message2
}
private fun queryNewestInternalId(a: Long, b: Long) : Int {
    synchronized(LOCK) {
        val message = queryNewestMessage(a, b)
        return if (message == null) 1 else message.internalId + 1
    }
}

internal fun insertMessage(from : Int, to :Int, data: String, type: MessageType ) : Message {
    // use a lock to avoid concurrent insert
    synchronized(LOCK) {
        val internalId = queryNewestInternalId(from.toLong(), to.toLong())
        serverMessageQueries.insert(
            from.toLong(),
            to.toLong(),
            data,
            type,
            System.currentTimeMillis() / 1000,
            internalId.toLong()
        )
        return serverMessageQueries.selectPrecise(
            from.toLong(),
            to.toLong(),
            internalId.toLong()
        ).executeAsOne().toMessage()
    }
}

internal fun Contact.toContact() : top.focess.netdesign.server.Contact = when (this.type.toInt()) {
        0 -> ServerFriend(this.id.toInt(), this.name)
        1 -> {
            val ids = groupQueries.selectMembers(this.id).executeAsList()
            Group(this.id.toInt(), this.name, true, ids.map { ServerMember(friendQueries.selectById(it).executeAsOne().toContact()) })
        }
        else -> throw IllegalArgumentException()
    }

internal fun top.focess.netdesign.sqldelight.contact.Friend.toContact() : Friend = ServerFriend(this.id.toInt(), this.name)
