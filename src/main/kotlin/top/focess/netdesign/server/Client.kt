package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import top.focess.netdesign.server.packet.ClientPacket
import top.focess.netdesign.server.packet.ServerPacket
import java.io.Closeable

abstract class Client : Closeable {

    var connected by mutableStateOf(ConnectionStatus.DISCONNECTED)

    var online by mutableStateOf(true)
    var registrable by mutableStateOf(false)
    var self: Friend? by mutableStateOf(null)

    var id: Int? = null
    var token: String? = null
    var username: String? = null


    abstract suspend fun sendPacket(clientPacket: ClientPacket) : ServerPacket?
    enum class ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING;

        operator fun invoke() = this == CONNECTED

        operator fun not() = this == DISCONNECTED
    }

}