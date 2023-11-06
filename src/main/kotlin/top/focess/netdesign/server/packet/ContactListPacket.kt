package top.focess.netdesign.server.packet

import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contact
import top.focess.netdesign.proto.contactListRequest
import top.focess.netdesign.proto.contactListResponse
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.Friend
import top.focess.netdesign.server.Group

data class ContactListResponsePacket(val contacts: List<Contact>) : ServerPacket(PACKET_ID) {
    companion object {
        val PACKET_ID = 9
    }

    override fun toProtoType(): GeneratedMessageV3 = contactListResponse {
        this.contacts.addAll(this@ContactListResponsePacket.contacts.map { contact ->
            contact {
                this.id = contact.id
                this.name = contact.name
                this.online = contact.online
                this.type = when (contact) {
                    is Friend -> PacketOuterClass.Contact.ContactType.FRIEND
                    is Group -> PacketOuterClass.Contact.ContactType.GROUP
                    else -> PacketOuterClass.Contact.ContactType.UNRECOGNIZED
                }
                if (contact is Group)
                    this.members.addAll(
                        contact.members.map {
                            contact {
                                this.id = it.id
                                this.name = it.name
                                this.online = it.online
                                this.type = PacketOuterClass.Contact.ContactType.MEMBER
                            }
                        }.toList()
                    )
            }
        }.toList())
    }
}

class ContactListRequestPacket : ClientPacket(PACKET_ID) {

    companion object {
        val PACKET_ID = 8
    }

    override fun toProtoType(): GeneratedMessageV3 = contactListRequest {
    }

}
