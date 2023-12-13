package top.focess.netdesign.server

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.sqldelight.message.ServerMessage
import top.focess.netdesign.ui.friendQueries
import top.focess.netdesign.ui.serverMessageQueries
import top.focess.netdesign.ui.sha256
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom

@OptIn(DelicateCoroutinesApi::class)
class SingleServer(val name: String, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

    private val clientScopeMap = mutableMapOf<String, ClientScope>()

    private val challengeMap = mutableMapOf<String, String>()

    private val defaultContact = Friend(0, name, true)

    private val serverSocket = ServerSocket(port)

    private var shouldClose = false

    init {

        Thread {
            while (true) {
                if (this.shouldClose)
                    break
                try {
                    val socket = serverSocket.accept()
                    BufferedInputStream(socket.getInputStream()).let {
                        val bytes = runBlocking { it.readAvailableBytes() }
                        val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                        val packetId = protoPacket.packetId
                        println("SingleServer: receive $packetId")
                        val packet = Packets.fromProtoPacket(protoPacket) as ClientPacket
                        if (packet is SetupChannelRequestPacket)
                            this.setupChannel(socket, packet.token)
                        with(DEFAULT_PACKET_HANDLER) {
                            this@SingleServer.handle(Packets.fromProtoPacket(protoPacket) as ClientPacket)
                        }
                    }?.let { packet ->
                        println("SingleServer: send ${packet.packetId}");
                        val bytes = packet.toProtoPacket().toByteArray()
                        socket.getOutputStream().let {
                            it.write(bytes)
                            it.flush()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun close() {
        shouldClose = true;
        serverSocket.close()
    }

    private fun setupChannel(socket : Socket, token : String) {
        val clientScope = this.clientScopeMap[token] ?: return
        clientScope.channelSocket = socket
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
                                        return LoginResponsePacket(true, token)
                                    }

                                }
                            }
                        }
                        LoginResponsePacket(false, "")
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
    }

    private interface SingleServerPacketHandler {
        fun SingleServer.handle(packet: ClientPacket): ServerPacket?
    }

    private fun sendChannelPacket(clientScope: ClientScope, packet: ServerPacket) {
        val socket = clientScope.channelSocket
        val bytes = packet.toProtoPacket().toByteArray()
        BufferedOutputStream(socket.getOutputStream()).let {
            it.write(bytes)
            it.flush()
        }
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

