package top.focess.netdesign.server

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.ui.friendQueries
import top.focess.netdesign.ui.serverMessageQueries
import top.focess.util.RSA
import java.io.BufferedInputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom

@OptIn(DelicateCoroutinesApi::class)
class SingleServer(val name: String, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

    private val defaultContact = Friend(0, name, true)

    private val serverSocket = ServerSocket(port)

    private var shouldClose = false

    private val ownRSAKey = RSA.genRSAKeypair()

    init {
        suspend fun handle(socket: Socket) {
            createClientScope(this) {
                if (shouldClose) {
                    break0()
                    return@createClientScope
                }
                try {
                    BufferedInputStream(socket.getInputStream()).let {
                        var bytes = it.readAvailableBytes()
                        if (this.clientPublicKey != null)
                            bytes = RSA.decryptRSA(bytes, ownRSAKey.privateKey)
                        val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                        val packetId = protoPacket.packetId
                        println("SingleServer: receive $packetId");
                        DEFAULT_PACKET_HANDLER.handle(Packets.fromProtoPacket(protoPacket) as ClientPacket)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e !is IllegalArgumentException)
                        break0()
                    null
                }?.let { packet ->
                    try {
                        println("SingleServer: send ${packet.packetId}");
                        var bytes = packet.toProtoPacket().toByteArray()
                        if (packet !is ServerStatusResponsePacket && this.clientPublicKey != null)
                            bytes = RSA.encryptRSA(bytes, this.clientPublicKey)
                        socket.getOutputStream().let {
                            it.write(bytes)
                            it.flush()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        break0()
                    }
                }

            }
        }

        Thread {
            while (true) {
                if (this.shouldClose)
                    break
                try {
                    val socket = serverSocket.accept()
                    GlobalScope.launch {
                        handle(socket)
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }

    override fun close() {
        shouldClose = true;
        serverSocket.close()
    }

    companion object {
        private suspend fun createClientScope(server: SingleServer, block: suspend ClientScope.() -> Unit) {
            val clientScope = ClientScope(server)
            while (!clientScope.shouldClose)
                clientScope.block()
        }

        private val DEFAULT_PACKET_HANDLER = object : SingleServerPacketHandler {

            override fun ClientScope.handle(packet: ClientPacket): ServerPacket? =
                when (packet) {
                    is ServerStatusRequestPacket -> {
                        this.clientPublicKey = packet.clientPublicKey
                        ServerStatusResponsePacket(
                            online = false,
                            registrable = false,
                            server.ownRSAKey.publicKey
                        )
                    }

                    is LoginPreRequestPacket -> {
                        this.username = packet.username
                        this.challenge = genChallenge()
                        LoginPreResponsePacket(
                            this.challenge!!
                        )
                    }

                    is LoginRequestPacket -> {
                        if (packet.username.length in 6..20) {
                            if (packet.username == this.username) {
                                val friend = friendQueries.selectByName(packet.username).executeAsOneOrNull()
                                val hashPassword = if (friend == null) {
                                    val password = packet.hashPassword.substring(
                                        0,
                                        packet.hashPassword.length - this.challenge!!.length
                                    )
                                    friendQueries.insert(packet.username, password)
                                    packet.hashPassword
                                } else if (friend.id.toInt() != 0)
                                    friend.password + this.challenge!!
                                else
                                    null
                                this.logined = hashPassword == packet.hashPassword
                                if (logined) {
                                    if (friend != null)
                                        this.self = Friend(friend.id.toInt(), friend.name, true)
                                    else {
                                        val f = friendQueries.selectByName(packet.username).executeAsOne()
                                        this.self = Friend(f.id.toInt(), f.name, true)
                                    }
                                }
                            }
                        } else
                            this.logined = false
                        this.username = null
                        LoginResponsePacket(this.logined)
                    }

                    is ServerStatusUpdateRequestPacket ->
                        ServerStatusUpdateResponsePacket(
                            online = false,
                            registrable = false
                        )

                    is ContactListRequestPacket ->
                        if (this.logined)
                            ContactListResponsePacket(
                                listOf(
                                    this.server.defaultContact,
                                    self!!
                                )
                            ) else null

                    is ContactRequestPacket ->
                        if (this.logined)
                            when (packet.id) {
                                0 -> ContactResponsePacket(this.server.defaultContact)
                                self!!.id -> ContactResponsePacket(self!!)
                                else -> null
                            }
                        else null

                    is ContactMessageRequestPacket ->
                        if (this.logined)
                            ContactMessageResponsePacket(
                                queryAllMessage(
                                    packet.id,
                                    this.self!!.id,
                                    packet.internalId
                                )
                            )
                        else null

                    is FriendSendMessageRequestPacket ->
                        if (this.logined) {
                            if (this.self!!.id == packet.from && 0 == packet.to && packet.content.length < 1000) {
                                val message = serverMessageQueries.selectNewestBySenderAndReceiver(
                                    packet.from.toLong(),
                                    packet.to.toLong()
                                ).executeAsOneOrNull()
                                val internalId = if (message == null) 0 else message.internal_id.toInt() + 1
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
                                ).executeAsOne()
                                FriendSendMessageResponsePacket(
                                    Message(
                                        insertedMessage.id.toInt(),
                                        packet.from,
                                        packet.to,
                                        internalId,
                                        when (packet.type) {
                                            MessageType.TEXT -> TextMessageContent(packet.content)
                                            MessageType.IMAGE -> ImageMessageContent(packet.content)
                                            MessageType.FILE -> FileMessageContent(packet.content)
                                        },
                                        insertedMessage.timestamp.toInt()
                                    )
                                )
                            } else null
                        }
                        else  null

                    else -> throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
                }

        }
    }


    private class ClientScope(val server: SingleServer) {

        var clientPublicKey: String? = null

        var username: String? = null
        var challenge: String? = null

        var logined = false
        var shouldClose = false
        var self: Contact? = null
        fun SingleServerPacketHandler.handle(packet: ClientPacket): ServerPacket? = this@ClientScope.handle(packet)

        fun break0() {
            shouldClose = true
        }
    }

    private interface SingleServerPacketHandler {
        fun ClientScope.handle(packet: ClientPacket): ServerPacket?
    }


}

private fun genChallenge(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun queryAllMessage(a: Int, b : Int, internalId: Int): List<Message> {
    val messages = serverMessageQueries.selectBySenderAndReceiverAndInternalId(a.toLong(),b.toLong(),internalId.toLong()).executeAsList().map{
        Message(
            it.id.toInt(),
            it.sender.toInt(),
            it.receiver_.toInt(),
            it.internal_id.toInt(),
            when (it.type) {
                MessageType.TEXT -> TextMessageContent(it.data_)
                MessageType.IMAGE -> ImageMessageContent(it.data_)
                MessageType.FILE -> FileMessageContent(it.data_)
            },
            it.timestamp.toInt()
        )
    }.toMutableList()
    messages.addAll(serverMessageQueries.selectBySenderAndReceiverAndInternalId(b.toLong(),a.toLong(),internalId.toLong()).executeAsList().map{
        Message(
            it.id.toInt(),
            it.sender.toInt(),
            it.receiver_.toInt(),
            it.internal_id.toInt(),
            when (it.type) {
                MessageType.TEXT -> TextMessageContent(it.data_)
                MessageType.IMAGE -> ImageMessageContent(it.data_)
                MessageType.FILE -> FileMessageContent(it.data_)
            },
            it.timestamp.toInt()
        )
    })
    return messages
}
