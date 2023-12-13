package top.focess.netdesign.server

import kotlinx.coroutines.*
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.sqldelight.message.ServerMessage
import top.focess.netdesign.ui.friendQueries
import top.focess.netdesign.ui.serverMessageQueries
import top.focess.netdesign.ui.sha256
import top.focess.scheduler.FocessScheduler
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(DelicateCoroutinesApi::class)
class SingleServer(val name: String, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

    private val clientScopeMap = mutableMapOf<String, ClientScope>()

    private val challengeMap = mutableMapOf<String, String>()

    private val defaultContact = Friend(0, name, true)

    private val serverSocket = ServerSocket(port)

    private var shouldClose = false

    private val scheduler : FocessScheduler = FocessScheduler("SingleServer")

    private val packetQueue = ConcurrentLinkedQueue<Pair<ClientScope, ServerPacket>>()

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
                runBlocking { sendChannelPacket0(clientScope, serverPacket) }
            }
        }
    }

    init {
        // 10 seconds to send a heart packet
        scheduler.runTimer(Runnable {
            clientScopeMap.forEach { (_, clientScope) -> sendChannelPacket(clientScope, ChannelHeartRequestPacket()) }
        }, Duration.ofSeconds(10), Duration.ZERO)

        Thread {
            while (!this.shouldClose) {
                GlobalScope.launch {
                    try {
                        val socket = serverSocket.accept()
                        BufferedInputStream(socket.getInputStream()).let {
                            withTimeout(3000) {
                                val bytes = it.readBytes()
                                val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                                val packetId = protoPacket.packetId
                                println("SingleServer: receive $packetId")
                                val clientPacket = Packets.fromProtoPacket(protoPacket) as ClientPacket
                                with(DEFAULT_PACKET_HANDLER) {
                                    this@SingleServer.handle(Packets.fromProtoPacket(protoPacket) as ClientPacket)
                                }?.to(clientPacket)
                            }
                        }?.let {
                            val serverPacket = it.first
                            println("SingleServer: send ${serverPacket.packetId}");
                            val bytes = serverPacket.toProtoPacket().toByteArray()
                            socket.getOutputStream().let {
                                it.write(bytes)
                                it.flush()
                            }
                            val clientPacket = it.second
                            if (clientPacket is SetupChannelRequestPacket)
                                setupChannel(socket, clientPacket.token)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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


    private fun setupChannel(socket : Socket, token : String) {
        // re setup channel is accepted because the channel will be closed if some error occurs
        val clientScope = this.clientScopeMap[token] ?: return
        clientScope.channelSocket = socket
        clientScope.isChannelSetup = true
        GlobalScope.launch {
            delay(200)
            sendChannelPacket(clientScope, ContactListRequestPacket(listOf(defaultContact)))
        }
    }

    companion object {

        private val DEFAULT_PACKET_HANDLER = object : SingleServerPacketHandler {

            override fun SingleServer.handle(packet: ClientPacket): ServerPacket? {
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
                                friendQueries.insert(packet.username, packet.rawPassword.sha256())
                                RegisterResponsePacket(true)
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
                                        this.clientScopeMap[token] = ClientScope(packet.username, token)
                                        return LoginResponsePacket(true, friend.id.toInt(), token)
                                    }

                                }
                            }
                        }
                        LoginResponsePacket(false, -1,"")
                    }

                    is ServerStatusUpdateRequestPacket ->
                        ServerStatusUpdateResponsePacket(
                            online = false,
                            registrable = true
                        )
                    is SetupChannelRequestPacket -> ServerAckResponsePacket()
                    is ContactMessageRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null) {
                            if (packet.id == 0 ) {
                                return ContactMessageResponsePacket(
                                   queryMessage(packet.id, clientScope.self.id, packet.internalId) ?: EMPTY_MESSAGE
                                )
                            }
                        }
                        null
                    }

                    is FriendSendMessageRequestPacket -> {
                        val clientScope = this.clientScopeMap[packet.token]
                        if (clientScope != null)
                            if (clientScope.self.id == packet.from && 0 == packet.to && packet.content.length < 1000) {
                                val message = queryNewestMessage(
                                    packet.from.toLong(),
                                    packet.to.toLong()
                                )
                                val internalId = if (message == null) 0 else message.internalId + 1
                                serverMessageQueries.insert(
                                    packet.from.toLong(),
                                    packet.to.toLong(),
                                    packet.content,
                                    packet.type,
                                    System.currentTimeMillis() / 1000,
                                    internalId.toLong()
                                )
                                val insertedMessage = serverMessageQueries.selectPrecise(
                                    packet.from.toLong(),
                                    packet.to.toLong(),
                                    internalId.toLong()
                                ).executeAsOne().toMessage()
                                return FriendSendMessageResponsePacket(
                                    insertedMessage
                                )
                            }
                        null
                    }

                    else -> throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
                }

            }
        }
    }


    private data class ClientScope(val username: String, val token: String, val self: Friend = Friend(0, username, true)) {
        lateinit var channelSocket: Socket
        var isChannelSetup = false
    }

    private interface SingleServerPacketHandler {
        fun SingleServer.handle(packet: ClientPacket): ServerPacket?
    }

    private suspend fun sendChannelPacket0(clientScope: ClientScope, packet: ServerPacket) {
        if (!clientScope.isChannelSetup) return
        if (clientScope.channelSocket.isClosed) {
            // channel is closed by os
            clientScope.isChannelSetup = false
            return
        }
        clientScope.channelSocket.use {
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
                clientScope.isChannelSetup = false
                clientScope.channelSocket.close()
            }
        }
    }

    private fun sendChannelPacket(clientScope: ClientScope, packet: ServerPacket) {
        this.packetQueue.add(clientScope to packet)
    }

    fun sendChannelPacket(token: String, packet: ServerPacket) { sendChannelPacket(this.clientScopeMap[token] ?: return, packet) }

    fun sendChannelPacketByUsername(username: String, packet: ServerPacket) { sendChannelPacket(this.clientScopeMap.values.find { it.username == username } ?: return, packet) }


}

private fun genChallenge(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun genToken() = genChallenge()

private fun queryMessage(a: Int, b : Int, internalId: Int): Message? {
    serverMessageQueries.selectPrecise(a.toLong(),b.toLong(),internalId.toLong()).executeAsOneOrNull()?.toMessage()?.let {
        return it
    }

    serverMessageQueries.selectPrecise(b.toLong(),a.toLong(),internalId.toLong()).executeAsOneOrNull()?.toMessage()?.let {
        return it
    }

    return null
}

private fun queryNewestMessage(a: Long, b : Long): Message? {
    serverMessageQueries.selectNewest(a,b).executeAsOneOrNull()?.toMessage()?.let {
        return it
    }

    serverMessageQueries.selectNewest(b,a).executeAsOneOrNull()?.toMessage()?.let {
        return it
    }

    return null
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

