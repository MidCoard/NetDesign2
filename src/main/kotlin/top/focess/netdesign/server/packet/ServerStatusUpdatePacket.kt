package top.focess.netdesign.server.packet

data class ServerStatusUpdateResponsePacket(val online: Boolean, val registrable: Boolean) : ServerPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 7
    }

    override fun toProtoType() = top.focess.netdesign.proto.serverStatusUpdateResponse {
        this.online = this@ServerStatusUpdateResponsePacket.online
        this.registrable = this@ServerStatusUpdateResponsePacket.registrable
    }
}

class ServerStatusUpdateRequestPacket() : ClientPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 6
    }

    override fun toProtoType() = top.focess.netdesign.proto.serverStatusUpdateRequest {
    }
}

