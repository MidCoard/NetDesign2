package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.clientAckResponse
import top.focess.netdesign.server.GlobalState.contacts
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.ui.localMessageQueries
import top.focess.netdesign.ui.queryLatestLocalMessages
import top.focess.util.RSA
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.InputStream
import java.net.Socket
import kotlin.math.max

class RemoteServer
internal constructor(host: String = NetworkConfig.DEFAULT_SERVER_HOST, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) :
    Closeable {

    var host by mutableStateOf(host)
    var port by mutableStateOf(port)
    var connected by mutableStateOf(ConnectionStatus.DISCONNECTED)

    var online by mutableStateOf(true)
    var registerable by mutableStateOf(false)
    var self: Friend? by mutableStateOf(null)

    var id: Int? = null
    var token: String? = null
    var username: String? = null

    private val socket: Socket
        get() = setupSocket()
    var channelSocket: Socket? = null
    private var channelThread: Thread? = null

    private fun setupSocket(): Socket {
        val socket = Socket(host, port)
        socket.soTimeout = 3000
        socket.tcpNoDelay = true
        return socket
    }

    private suspend fun trySendPacket(packet: ClientPacket, socket: Socket): ServerPacket? {
        println("RemoteServer: pre-send ${packet.packetId}")
        if (this.connected() && packet is ServerStatusRequestPacket)
        // server is connected, no need to send
        // if you want to update the server status, send an update packet or reconnect
            return ServerStatusResponsePacket(online, registerable)
        try {
            socket.let {
                val bytes = packet.toProtoPacket().toByteArray()
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
                // call and wait for 3000ms
                withTimeout(3000) {
                    val bytes = it.getInputStream().readAvailableBytes()
                    val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                    val packetId = protoPacket.packetId
                    println("RemoteServer: receive $packetId")
                    it.shutdownInput()
                    it.close()
                    Packets.fromProtoPacket(protoPacket) as ServerPacket
                }
            }
        } catch (e: Exception) {
            // trySendPacket method will catch this exception and return null
            e.printStackTrace()
            if (e is TimeoutCancellationException)
                println("RemoteServer: receive timeout")
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


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun setupChannel(id: Int, username: String, token: String, count: Int = 0) {
        if (count > 3) {
            disconnect()
            throw IllegalArgumentException("Setup channel failed")
        }
        if (!connected()) {
            this.channelSocket?.close()
            this.channelThread?.join()
            this.channelSocket = null
            return
        }
        this.channelSocket?.close()
        this.channelThread?.join()
        this.channelSocket = this.socket
        this.channelSocket?.keepAlive = true
        this.channelThread = Thread {
            // send channel setup request
            this.channelSocket?.let {
                BufferedOutputStream(it.getOutputStream()).let {
                    it.write(SetupChannelRequestPacket(token).toProtoPacket().toByteArray())
                    it.flush()
                }

                runBlocking {
                    try {
                        withTimeout(3000) {
                            val bytes = it.getInputStream().readAvailableBytes()
                            val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                            val packet = Packets.fromProtoPacket(protoPacket) as ServerPacket
                            if (packet !is ServerAckResponsePacket)
                                throw IllegalArgumentException("Setup channel failed")
                            this@RemoteServer.id = id
                            this@RemoteServer.token = token
                            this@RemoteServer.username = username
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        it.close()
                    }
                }
            }

            try {
                // start channel
                while (this.channelSocket?.isClosed?.not() == true) {
                    val bytes = runBlocking { channelSocket?.getInputStream()?.readAvailableBytes() }
                    if (bytes != null) {
                        val protoPacket = PacketOuterClass.Packet.parseFrom(bytes)
                        val packet = Packets.fromProtoPacket(protoPacket) as ServerPacket
                        println("RemoteServerChannel: receive ${packet.packetId}")
                        channelSocket?.getOutputStream()?.let {
                            BufferedOutputStream(it).let {
                                it.write(ClientAckResponsePacket().toProtoPacket().toByteArray())
                                it.flush()
                            }
                        }
                        with(DEFAULT_PACKET_HANDLER) {
                            handle(packet)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            println("RemoteServerChannel: channel closed")
            // channel closed
            // need to re setup channel.
            // channel will be closed by network error or actively closed by server if client no response to server or server do not receive client ack response
            GlobalScope.launch {
                delay(3000)
                setupChannel(id, username, token, count + 1)
            }
        }
        this.channelThread?.start()

    }

    override fun close() {
        this.disconnect()
    }

    suspend fun connect() {
        if (this.connected())
            return
        this.connected = ConnectionStatus.CONNECTING
        try {
            val packet = this.trySendPacket(ServerStatusRequestPacket(), this.socket)
            if (packet != null && packet is ServerStatusResponsePacket) {
                this.online = packet.online
                this.registerable = packet.registrable
                this.connected = ConnectionStatus.CONNECTED
            } else
                this.connected = ConnectionStatus.DISCONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
            // this exception is caused by this.socket's getter
            this.connected = ConnectionStatus.DISCONNECTED
        }
    }

    fun disconnect() {
        this.connected = ConnectionStatus.DISCONNECTED
        this.online = true
        this.registerable = false
        this.self = null
        this.channelSocket?.close()
        this.channelThread?.join()
        this.channelSocket = null
    }

    suspend fun reconnect() {
        if (this.connected()) {
            val packet = this.sendPacket(ServerStatusUpdateRequestPacket(), this.socket)
            if (packet is ServerStatusUpdateResponsePacket) {
                this.online = packet.online
                this.registerable = packet.registrable
            } else disconnect()
        } else {
            this.disconnect()
            this.connect()
        }
    }

    companion object {

        @OptIn(DelicateCoroutinesApi::class)
        private val DEFAULT_PACKET_HANDLER = object : RemoteServerPacketHandler {
            override fun RemoteServer.handle(packet: ServerPacket) {
                when (packet) {
                    is ChannelHeartRequestPacket -> {}
                    is ContactListRequestPacket -> {
                        contacts.clear()

                        for (i in 0 until packet.contacts.size) {
                            val contact = packet.contacts[i]
                            val internalId = packet.internalIds[i]
                            val localMessages = queryLatestLocalMessages(contact.id.toLong(), id!!.toLong())
                            contact.messages.addAll(localMessages)
                            val missingInternalIds = (max(1, internalId - 10)..internalId).toSet().subtract(localMessages.map { it.internalId }.toSet())
                            for (missingInternalId in missingInternalIds)
                                GlobalScope.launch {
                                    val contactMessageRequestPacket =
                                        sendPacket(ContactMessageRequestPacket(token!!, contact.id, missingInternalId))
                                    if (contactMessageRequestPacket is ContactMessageResponsePacket) {
                                        val message = contactMessageRequestPacket.message
                                        if (message.id != -1) {
                                            localMessageQueries.insert(
                                                message.id.toLong(),
                                                message.from.toLong(),
                                                message.to.toLong(),
                                                message.content.data,
                                                message.content.type,
                                                message.timestamp.toLong(),
                                                message.internalId.toLong(),
                                            )
                                            contact.messages.add(message)
                                        }
                                    }
                                }
                            contacts.add(contact)
                        }

                        this@handle.self = getContact(id!!) as Friend
                    }
                    is ContactRequestPacket -> {
                        if (packet.delete)
                            contacts.removeIf { it.id == packet.contact.id }
                        else contacts.add(packet.contact)
                    }
                    is ContactMessageListRequestPacket -> {
                        packet.messages.forEach {
                            val contact = getContact(it.from)
                            localMessageQueries.insert(
                                it.id.toLong(),
                                it.from.toLong(),
                                it.to.toLong(),
                                it.content.data,
                                it.content.type,
                                it.timestamp.toLong(),
                                it.internalId.toLong(),
                            )
                            contact?.messages?.add(it)
                        }
                    }
                    else -> throw IllegalArgumentException("Unknown packet id ${packet.packetId}")
                }
            }

        }
    }

    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING;

        operator fun invoke() = this == CONNECTED

        operator fun not() = this == DISCONNECTED
    }



    private interface RemoteServerPacketHandler {
        fun RemoteServer.handle(packet: ServerPacket)
    }

}

internal suspend fun InputStream.readAvailableBytes(): ByteArray {
    while (this.available() == 0)
        delay(1)
    val bytes = ByteArray(this.available())
    this.read(bytes)
    return bytes
}
