package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.util.RSA
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.InputStream
import java.net.Socket

class RemoteServer
internal constructor(host: String = NetworkConfig.DEFAULT_SERVER_HOST, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) :
    Closeable {

    var host by mutableStateOf(host)
    var port by mutableStateOf(port)
    var connected by mutableStateOf(ConnectionStatus.DISCONNECTED)

    var online by mutableStateOf(true)
    var registerable by mutableStateOf(false)
    var self: Friend? = null
    var token: String? = null
    var username: String? = null
    private var serverPublicKey: String? = null

    private val socket: Socket
        get() = setupSocket()
    private var channelSocket: Socket? = null
    private var channelThread: Thread? = null
    val channelPackets = mutableListOf<ServerPacket>()
    private val ownRSAKey = RSA.genRSAKeypair()

    private fun setupSocket(): Socket {
        val socket = Socket(host, port)
        socket.soTimeout = 1000
        socket.tcpNoDelay = true
        return socket
    }

    private suspend fun trySendPacket(packet: ClientPacket, socket: Socket): ServerPacket? {
        println("RemoteServer: pre-send ${packet.packetId}")
        if (this.connected() && packet is ServerStatusRequestPacket)
        // server is connected, no need to send
        // if you want to update the server status, send an update packet or reconnect
            return ServerStatusResponsePacket(online, registerable, serverPublicKey)
        try {
            socket.let {
                var bytes = packet.toProtoPacket().toByteArray()
                if (serverPublicKey != null)
                    bytes = RSA.encryptRSA(bytes, serverPublicKey)
                println("RemoteServer: send ${packet.packetId}")
                it.getOutputStream().write(bytes)
                it.shutdownOutput()
            }
        } catch (e: Exception) {
            // trySendPacket method will catch this exception and return null
            e.printStackTrace()
            return null
        }
        return try {
            socket.let {
                var bytes = it.getInputStream().readBytes()
                if (this.serverPublicKey != null)
                    bytes = RSA.decryptRSA(bytes, this.ownRSAKey.privateKey)
                val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                val packetId = protoPacket.packetId
                println("RemoteServer: receive $packetId")
                it.shutdownInput()
                it.close()
                Packets.fromProtoPacket(protoPacket) as ServerPacket
            }
        } catch (e: Exception) {
            // trySendPacket method will catch this exception and return null
            e.printStackTrace()
            null
        }
    }

    suspend fun sendPacket(packet: ClientPacket, socket: Socket = this.socket): ServerPacket? {
        if (!connected)
            return null
        val serverPacket = trySendPacket(packet, socket)
        if (serverPacket == null)
            disconnect()
        return serverPacket
    }


    suspend fun setupChannel(username: String, token: String) {
        if (!connected())
            return
        this.channelSocket?.close()
        this.channelThread?.join()
        this.channelSocket = this.socket
        this.channelSocket?.keepAlive = true
        this.channelThread = Thread {
            // send channel setup request
            this.channelSocket?.use {
                BufferedOutputStream(it.getOutputStream()).use {
                    it.write(SetupChannelRequestPacket(token).toProtoPacket().toByteArray())
                    it.flush()
                }

                try {
                    val bytes = runBlocking { it.getInputStream().readAvailableBytes() }
                    val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                    val packet = Packets.fromProtoPacket(protoPacket) as ServerPacket
                    if (packet !is ServerAckResponse)
                        throw IllegalArgumentException("Invalid packet")
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.close()
                    return@Thread
                }
            }

            // start channel
            while (this.channelSocket?.isClosed?.not() == true) {
                val bytes = runBlocking { channelSocket?.getInputStream()?.readAvailableBytes() }
                if (bytes != null) {
                    val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                    val packet = Packets.fromProtoPacket(protoPacket) as ServerPacket
                    this.channelPackets.add(packet)
                }
            }
        }
        val packet = this.sendPacket(SetupChannelRequestPacket(token), this.channelSocket!!)
        if (packet != null && packet is ServerAckResponse) {
            this.token = token
            this.username = username
        } else this.channelSocket?.close()

    }

    override fun close() {
        this.connected = ConnectionStatus.DISCONNECTED
        this.socket.close()
    }

    suspend fun connect() {
        if (this.connected())
            return
        this.connected = ConnectionStatus.CONNECTING
        val packet = this.trySendPacket(ServerStatusRequestPacket(ownRSAKey.publicKey), this.socket)
        if (packet != null && packet is ServerStatusResponsePacket) {
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

internal suspend fun InputStream.readAvailableBytes(): ByteArray {
    while (this.available() == 0)
        delay(1)
    val bytes = ByteArray(this.available())
    this.read(bytes)
    return bytes
}
