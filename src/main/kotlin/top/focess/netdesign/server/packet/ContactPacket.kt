package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contact
import top.focess.netdesign.proto.contactRequest
import top.focess.netdesign.proto.contactResponse
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.Friend
import top.focess.netdesign.server.Group
import top.focess.netdesign.server.Member

data class ContactResponsePacket(val contact: Contact) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactResponsePacket>() {
        override val PACKET_ID = 11
        override fun fromProtoType(packet: Any): ContactResponsePacket {
            val contactResponse: PacketOuterClass.ContactResponse = packet.unpack()
            val contact = contactResponse.contact
            return ContactResponsePacket(
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
    }

    override fun toProtoType() = contactResponse {
        this.contact = contact {
            this.id = this@ContactResponsePacket.contact.id;
            this.name = this@ContactResponsePacket.contact.name
            this.type = when (this@ContactResponsePacket.contact) {
                is Friend -> PacketOuterClass.Contact.ContactType.FRIEND
                is Group -> PacketOuterClass.Contact.ContactType.GROUP
                is Member -> PacketOuterClass.Contact.ContactType.MEMBER
                else -> PacketOuterClass.Contact.ContactType.UNRECOGNIZED
            }
            if (this@ContactResponsePacket.contact is Group)
                this.members.addAll(
                    this@ContactResponsePacket.contact.members.map {
                        contact {
                            this.id = it.id
                            this.name = it.name
                            this.type = PacketOuterClass.Contact.ContactType.MEMBER
                        }
                    }.toList()
                )
        }
    }
}

data class ContactRequestPacket(val id: Int) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactRequestPacket>() {
        override val PACKET_ID = 10
        override fun fromProtoType(packet: Any): ContactRequestPacket {
            val contactRequest: PacketOuterClass.ContactRequest = packet.unpack()
            return ContactRequestPacket(contactRequest.id)
        }
    }
    override fun toProtoType() = contactRequest {
        this.id = this@ContactRequestPacket.id
    }
}