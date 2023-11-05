package top.focess.netdesign.server

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.util.RSA
import java.io.BufferedInputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

@OptIn(DelicateCoroutinesApi::class)
class SingleServer(val name: String, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

    private var friendId = AtomicInteger(1)

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
                        val packetId = PacketOuterClass.Packet.parseFrom(bytes).packetId
                        println("SingleServer: receive $packetId");
                        when (packetId) {
                            0 -> {
                                val serverStatusRequest = PacketOuterClass.ServerStatusRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    ServerStatusRequestPacket(
                                        serverStatusRequest.clientPublicKey
                                    )
                                )
                            }

                            2 -> {
                                val loginPreRequest = PacketOuterClass.LoginPreRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    LoginPreRequestPacket(
                                        loginPreRequest.username
                                    )
                                )
                            }

                            4 -> {
                                val loginRequest = PacketOuterClass.LoginRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    LoginRequestPacket(
                                        loginRequest.username,
                                        loginRequest.hashPassword
                                    )
                                )
                            }

                            6 -> {
                                DEFAULT_PACKET_HANDLER.handle(
                                    ServerStatusUpdateRequestPacket()
                                )
                            }

                            8 -> {
                                DEFAULT_PACKET_HANDLER.handle(
                                    ContactListRequestPacket()
                                )
                            }

                            10 -> {
                                val friendInfoRequestPacket = PacketOuterClass.FriendInfoRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    FriendInfoRequestPacket(
                                        friendInfoRequestPacket.id
                                    )
                                )
                            }

                            12 -> {
                                val groupInfoRequestPacket = PacketOuterClass.GroupInfoRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    GroupInfoRequestPacket(
                                        groupInfoRequestPacket.id
                                    )
                                )
                            }

                            else -> throw IllegalArgumentException("Unknown packet id: $packetId")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e !is IllegalArgumentException)
                        break0()
                    null
                }?.let { packet ->
                    try {
                        println("SingleServer: send ${packet.packetId}");
                        var bytes = packet.toProtoType().toByteArray()
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
                        LoginPreResponsePacket(
                            genChallenge()
                        )
                    }
                    is LoginRequestPacket -> {
                        this.logined = this.username == packet.username
                        if (packet.username.length < 6 || packet.username.length > 20)
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
                                ContactInfo(0, this@handle.server.name, PacketOuterClass.Contact.ContactType.FRIEND)
                            )
                        ) else null

                    is FriendInfoRequestPacket ->
                        if (this.logined)
                            if (packet.id == 0)
                                FriendInfoResponsePacket(
                                    packet.id,
                                    this.server.name
                                )
                            else FriendInfoResponsePacket(-1, "")
                        else null

                    is GroupInfoRequestPacket ->
                        if (this.logined)
                            GroupInfoResponsePacket(-1, "", listOf())
                        else null

                    else -> throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
                }

        }
    }


    private class ClientScope(val server: SingleServer) {

        var clientPublicKey: String? = null

        var username: String? = null

        var logined = false
        var shouldClose = false
        fun SingleServerPacketHandler.handle(packet: ClientPacket): ServerPacket? = this@ClientScope.handle(packet)

        fun break0() {
            shouldClose = true
        }
    }

    private interface SingleServerPacketHandler {
        fun ClientScope.handle(packet: ClientPacket): ServerPacket?
    }


}

internal fun genChallenge(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
