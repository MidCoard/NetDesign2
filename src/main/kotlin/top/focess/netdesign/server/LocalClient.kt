package top.focess.netdesign.server

import top.focess.netdesign.server.GlobalState.localServer
import top.focess.netdesign.server.LocalServer.Companion.DEFAULT_PACKET_HANDLER
import top.focess.netdesign.server.packet.ClientPacket
import top.focess.netdesign.server.packet.ServerPacket

class LocalClient(online: Boolean, registrable: Boolean, self: Friend, token: String) : Client() {

    init {
        this.connected = ConnectionStatus.CONNECTED
        this.online = online
        this.registrable = registrable
        this.self = self
        this.id = self.id
        this.username = self.name
        this.token = token
    }
    override suspend fun sendPacket(clientPacket: ClientPacket): ServerPacket? {
        return localServer?.let {
            with(DEFAULT_PACKET_HANDLER) {
                it.handle(clientPacket)
            }
        }
    }

    override fun close() {}
}