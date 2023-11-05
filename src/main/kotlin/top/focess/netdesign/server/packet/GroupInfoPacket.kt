package top.focess.netdesign.server.packet

import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contact
import top.focess.netdesign.server.Member

data class GroupInfoResponsePacket(val id: Int, val name: String, val members: List<Member>) : ServerPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 13
    }

    override fun toProtoType() = top.focess.netdesign.proto.groupInfoResponse {
        this.packetId = PACKET_ID
        this.id = this@GroupInfoResponsePacket.id
        this.name = this@GroupInfoResponsePacket.name
        this.members.addAll(this@GroupInfoResponsePacket.members.map {
            contact {
                this.id = it.id
                this.name = it.name
                this.type = PacketOuterClass.Contact.ContactType.MEMBER
            }
        }.toList())
    }
}

data class GroupInfoRequestPacket(val id: Int) : ClientPacket(PACKET_ID) {
    companion object {
        const val PACKET_ID = 12
    }

    override fun toProtoType() = top.focess.netdesign.proto.groupInfoRequest {
        this.packetId = PACKET_ID
        this.id = this@GroupInfoRequestPacket.id
    }
}