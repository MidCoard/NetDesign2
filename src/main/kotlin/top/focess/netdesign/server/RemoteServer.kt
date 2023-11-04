package top.focess.netdesign.server

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import kotlinx.coroutines.*
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass.Packet
import top.focess.netdesign.proto.PacketOuterClass.ServerStatusResponse
import top.focess.netdesign.server.packet.ClientPacket
import top.focess.netdesign.server.packet.ServerPacket
import top.focess.netdesign.server.packet.ServerStatusPacket
import top.focess.netdesign.server.packet.ServerStatusRequestPacket
import top.focess.util.RSA
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.DataOutputStream
import java.io.InputStream
import java.net.Socket
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class RemoteServer
internal constructor(host: String = NetworkConfig.DEFAULT_SERVER_HOST, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) : Closeable {

    var host by mutableStateOf(host)
    var port by mutableStateOf(port)
    var connected by mutableStateOf(ConnectionStatus.DISCONNECTED)

    var online by mutableStateOf(true)
    var registerable by mutableStateOf(false)
    private var serverPublicKey: String? = null

    private lateinit var socket: Socket
    private val ownRSAKey = RSA.genRSAKeypair()

    private suspend fun trySendPacket(packet: ClientPacket) : ServerPacket? {
        if (this.connected() && packet is ServerStatusRequestPacket)
            // server is connected, no need to send
            // if you want to update the server status, send an update packet or reconnect
            return ServerStatusPacket(this.online, this.registerable, this.serverPublicKey)
        try {
            val bytes = packet.toProtoType().toByteArray()
            if (serverPublicKey != null)
                RSA.encryptRSA(bytes, serverPublicKey)
            BufferedOutputStream(socket.getOutputStream()).let {
                it.write(bytes)
                it.flush()
            }
        } catch (e :Exception) {
            e.printStackTrace()
            return null
        }

        return try {
            socket.getInputStream().let {
                var bytes = it.readAvailableBytes()
                if (this.serverPublicKey != null)
                    bytes = RSA.decryptRSA(bytes, this.serverPublicKey)
                when (val packetId = Packet.parseFrom(bytes).packetId) {
                    1 -> {
                        val serverStatusResponse = ServerStatusResponse.parseFrom(bytes)
                        ServerStatusPacket(
                            serverStatusResponse.online,
                            serverStatusResponse.registrable,
                            serverStatusResponse.serverPublicKey
                        )
                    }

                    3 -> {
                        TODO()
                    }

                    else -> throw IllegalArgumentException("Unknown packet id: $packetId")
                }
            }
        } catch (e :Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sendPacket(packet: ClientPacket) : ServerPacket? {
        if (!connected())
            return null
        val serverPacket = trySendPacket(packet)
        if (serverPacket == null)
            connected = ConnectionStatus.DISCONNECTED
        return serverPacket
    }

    override fun close() {
        this.connected = ConnectionStatus.DISCONNECTED
        this.socket.close()
    }

    suspend fun connect() {
        if (this.connected())
            return
        this.connected = ConnectionStatus.CONNECTING
        try {
            socket = Socket(host, port)
            socket.keepAlive = true
            socket.soTimeout = 1000
            socket.tcpNoDelay = true
        } catch (e :Exception) {
            e.printStackTrace()
            this.connected = ConnectionStatus.DISCONNECTED
            return
        }
        val packet = this.trySendPacket(ServerStatusRequestPacket(ownRSAKey.publicKey))
        delay(2000)
        if (packet != null && packet is ServerStatusPacket) {
            this.online = packet.online
            this.registerable = packet.registrable
            this.serverPublicKey = packet.serverPublicKey
            this.connected = ConnectionStatus.CONNECTED
        } else
            this.connected = ConnectionStatus.DISCONNECTED
    }

    fun disconnect() {
        this.connected = ConnectionStatus.DISCONNECTED
        this.online = true
        this.registerable = false
        this.serverPublicKey = null
        this.socket.close()
    }

    suspend fun reconnect() {
        this.disconnect()
        this.connect()
    }

    companion object {
        fun Saver() = listSaver(
            save = { listOf(it.host, it.port) },
            restore = {
                RemoteServer(it[0] as String, it[1] as Int).apply {
                    this.connected = ConnectionStatus.DISCONNECTED
                }
            }
        )
    }

    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING;
        operator fun invoke() = this == CONNECTED

        operator fun not() = this == DISCONNECTED
    }

}

suspend fun InputStream.readAvailableBytes() : ByteArray {
    while (this.available() == 0)
        delay(1)
    val bytes = ByteArray(this.available())
    this.read(bytes)
    return bytes
}