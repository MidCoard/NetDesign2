package top.focess.netdesign.server.packet

import top.focess.netdesign.proto.serverStatusRequest
import top.focess.netdesign.proto.serverStatusResponse

data class ServerStatusPacket(val online: Boolean, val registrable: Boolean, val serverPublicKey: String?) : ServerPacket(PACKET_ID) {

    companion object {
        const val PACKET_ID = 1
    }

    override fun toProtoType() = serverStatusResponse {
        this.packetId = PACKET_ID
        this.online = this@ServerStatusPacket.online
        this.registrable = this@ServerStatusPacket.registrable
        this.serverPublicKey = this@ServerStatusPacket.serverPublicKey
    }
}

data class ServerStatusRequestPacket(val clientPublicKey: String?) : ClientPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 0
    }

    override fun toProtoType() = serverStatusRequest {
        this.packetId = PACKET_ID
        this.clientPublicKey = this@ServerStatusRequestPacket.clientPublicKey
    }


}

