package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.focess.netdesign.config.NetworkConfig
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.packet.*
import top.focess.util.RSA
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.InputStream
import java.net.Socket

@OptIn(DelicateCoroutinesApi::class)
class RemoteServer
internal constructor(host: String = NetworkConfig.DEFAULT_SERVER_HOST, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) :
    Closeable {

    var host by mutableStateOf(host)
    var port by mutableStateOf(port)
    var connected by mutableStateOf(ConnectionStatus.DISCONNECTED)

    var online by mutableStateOf(true)
    var registerable by mutableStateOf(false)
    var lastLoginedUsername: String? = null
    private var serverPublicKey: String? = null

    private lateinit var socket: Socket
    private val ownRSAKey = RSA.genRSAKeypair()
    private val mutex = Mutex()

    private suspend fun trySendPacket(packet: ClientPacket): ServerPacket? {
        mutex.withLock {
            println("RemoteServer: send ${packet.packetId}")
            if (this.connected() && packet is ServerStatusRequestPacket)
            // server is connected, no need to send
            // if you want to update the server status, send an update packet or reconnect
                return ServerStatusResponsePacket(online, registerable, serverPublicKey)
            try {
                if (packet is LoginRequestPacket)
                    lastLoginedUsername = packet.username
                var bytes = packet.toProtoType().toByteArray()
                if (serverPublicKey != null)
                    bytes = RSA.encryptRSA(bytes, serverPublicKey)
                BufferedOutputStream(socket.getOutputStream()).let {
                    it.write(bytes)
                    it.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }


            return socket.getInputStream().let {
                var bytes = it.readAvailableBytes()
                if (this.serverPublicKey != null)
                    bytes = RSA.decryptRSA(bytes, this.ownRSAKey.privateKey)
                val packetId = PacketOuterClass.Packet.parseFrom(bytes).packetId
                println("RemoteServer: receive $packetId")
                when (packetId) {
                    1 -> {
                        val serverStatusResponse = PacketOuterClass.ServerStatusResponse.parseFrom(bytes)
                        ServerStatusResponsePacket(
                            serverStatusResponse.online,
                            serverStatusResponse.registrable,
                            serverStatusResponse.serverPublicKey
                        )
                    }

                    3 -> {
                        val loginPreResponse = PacketOuterClass.LoginPreResponse.parseFrom(bytes)
                        LoginPreResponsePacket(
                            loginPreResponse.challenge
                        )
                    }

                    5 -> {
                        val loginResponse = PacketOuterClass.LoginResponse.parseFrom(bytes)
                        LoginResponsePacket(
                            loginResponse.logined
                        )
                    }

                    7 -> {
                        val serverStatusUpdateResponse = PacketOuterClass.ServerStatusUpdateResponse.parseFrom(bytes)
                        ServerStatusUpdateResponsePacket(
                            serverStatusUpdateResponse.online,
                            serverStatusUpdateResponse.registrable,
                        )
                    }

                    9 -> {
                        val contactListResponse = PacketOuterClass.ContactListResponse.parseFrom(bytes)
                        ContactListResponsePacket(
                            contactListResponse.contactsList.map { contact ->
                                when (contact.type) {
                                    PacketOuterClass.Contact.ContactType.FRIEND -> Friend(
                                        contact.id,
                                        contact.name,
                                        contact.online
                                    )

                                    PacketOuterClass.Contact.ContactType.GROUP -> Group(
                                        contact.id,
                                        contact.name,
                                        contact.online,
                                        contact.membersList.map { member ->
                                            Member(member.id, member.name, member.online)
                                        }.toList()
                                    )

                                    else -> throw IllegalArgumentException("Unknown contact type: ${contact.type}")
                                }
                            }.toList()
                        )
                    }

                    11 -> {
                        val contactResponse = PacketOuterClass.ContactResponse.parseFrom(bytes)
                        val contact = contactResponse.contact
                        ContactResponsePacket(
                            when (contact.type) {
                                PacketOuterClass.Contact.ContactType.FRIEND -> Friend(
                                    contact.id,
                                    contact.name,
                                    contact.online
                                )

                                PacketOuterClass.Contact.ContactType.GROUP -> Group(
                                    contact.id,
                                    contact.name,
                                    contact.online,
                                    contact.membersList.map { member ->
                                        Member(member.id, member.name, member.online)
                                    }.toList()
                                )

                                else -> throw IllegalArgumentException("Unknown contact type: ${contact.type}")
                            }
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown packet id: $packetId")
                    }
                }
            }
        }
    }

    suspend fun sendPacket(packet: ClientPacket): ServerPacket? {
        if (!connected)
            return null
        var exception: Exception? = null
        val serverPacket = try {
            trySendPacket(packet)
        } catch (e: Exception) {
            exception = e;
            e.printStackTrace()
            null
        }
        if (serverPacket == null && (exception == null || exception !is IllegalArgumentException))
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
        } catch (e: Exception) {
            e.printStackTrace()
            this.connected = ConnectionStatus.DISCONNECTED
            return
        }
        val packet = this.trySendPacket(ServerStatusRequestPacket(ownRSAKey.publicKey))
        delay(2000)
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

internal suspend fun InputStream.readAvailableBytes(): ByteArray {
    while (this.available() == 0)
        delay(1)
    val bytes = ByteArray(this.available())
    this.read(bytes)
    return bytes
}