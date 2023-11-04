package top.focess.netdesign.server

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.PacketOuterClass.ServerStatusRequest
import top.focess.netdesign.server.packet.ClientPacket
import top.focess.netdesign.server.packet.ServerPacket
import top.focess.netdesign.server.packet.ServerStatusPacket
import top.focess.netdesign.server.packet.ServerStatusRequestPacket
import top.focess.util.RSA
import java.io.BufferedInputStream
import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket

@OptIn(DelicateCoroutinesApi::class)
class SingleServer(port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

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
                            bytes = RSA.decryptRSA(bytes, this.clientPublicKey)
                        when (val packetId = PacketOuterClass.Packet.parseFrom(bytes).packetId) {
                            0 -> {
                                val serverStatusRequest = ServerStatusRequest.parseFrom(bytes)
                                DEFAULT_PACKET_HANDLER.handle(
                                    ServerStatusRequestPacket(
                                        serverStatusRequest.clientPublicKey
                                    )
                                )
                            }

                            2 -> {
                                TODO()
                            }

                            else -> throw IllegalArgumentException("Unknown packet id: $packetId")
                        }
                    }
                } catch (e: Exception) {
                    if (e !is IllegalStateException)
                        break0()
                    null
                }?.let { packet ->
                    socket.getOutputStream().let {
                        it.write(packet.toProtoType().toByteArray())
                        it.flush()
                    }
                    socket.shutdownOutput()
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

            override fun ClientScope.handle(packet: ClientPacket): ServerPacket =
                when (packet) {
                    is ServerStatusRequestPacket -> ServerStatusPacket(
                        online = false,
                        registrable = false,
                        this@handle.server.ownRSAKey.publicKey
                    )

                    else -> throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
                }

        }
    }


    private class ClientScope(val server: SingleServer) {

        var shouldClose = false
        var clientPublicKey: String? = null
        var connected = false // logined ....
        fun SingleServerPacketHandler.handle(packet: ClientPacket): ServerPacket = this@ClientScope.handle(packet)

        fun break0() {
            shouldClose = true
        }
    }

    private interface SingleServerPacketHandler {
        fun ClientScope.handle(packet: ClientPacket): ServerPacket
    }


}
