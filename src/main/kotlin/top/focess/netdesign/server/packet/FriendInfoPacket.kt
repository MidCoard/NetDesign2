package top.focess.netdesign.server.packet

data class FriendInfoResponsePacket(val id: Int, val name: String) : ServerPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 11
    }

    override fun toProtoType() = top.focess.netdesign.proto.friendInfoResponse {
        this.packetId = PACKET_ID
        this.id = this@FriendInfoResponsePacket.id
        this.name = this@FriendInfoResponsePacket.name
    }
}

data class FriendInfoRequestPacket(val id: Int) : ClientPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 10
    }

    override fun toProtoType() = top.focess.netdesign.proto.friendInfoRequest {
        this.packetId = PACKET_ID
        this.id = this@FriendInfoRequestPacket.id
    }
}