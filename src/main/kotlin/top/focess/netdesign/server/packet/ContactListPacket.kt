package top.focess.netdesign.server.packet

import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contactListRequest
import top.focess.netdesign.proto.contactListResponse

data class ContactListResponsePacket(val contacts: List<ContactInfo>) : ServerPacket(PACKET_ID) {
    companion object {
        val PACKET_ID = 9
    }

    override fun toProtoType(): GeneratedMessageV3 = contactListResponse {
        this.packetId = PACKET_ID
        this.contacts.addAll(this@ContactListResponsePacket.contacts.map { contact ->
            top.focess.netdesign.proto.contact {
                this.id = contact.id
                this.name = contact.name
                this.online = contact.online
                this.type = contact.type
            }
        }.toList())
    }
}

class ContactListRequestPacket : ClientPacket(PACKET_ID) {

    companion object {
        val PACKET_ID = 8
    }

    override fun toProtoType(): GeneratedMessageV3 = contactListRequest {
        this.packetId = PACKET_ID
    }

}

data class ContactInfo(
    val id: Int,
    val name: String,
    val online: Boolean,
    val type: PacketOuterClass.Contact.ContactType,
)

