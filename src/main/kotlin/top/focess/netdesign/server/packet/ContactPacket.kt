package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contact
import top.focess.netdesign.proto.contactRequest
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.Friend
import top.focess.netdesign.server.Group
import top.focess.netdesign.server.Member

data class ContactRequestPacket(val contact: Contact, val delete: Boolean) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactRequestPacket>() {
        override val PACKET_ID = 10
        override fun fromProtoType(packet: Any): ContactRequestPacket {
            val contactResponse: PacketOuterClass.ContactRequest = packet.unpack()
            return ContactRequestPacket(contactResponse.contact.fromProtoType(), contactResponse.delete)
        }
    }

    override fun toProtoType() = contactRequest {
        this.contact = this@ContactRequestPacket.contact.toProtoType()
        this.delete = this@ContactRequestPacket.delete
    }
}

internal fun Contact.toProtoType() = contact {
    this.id = this@toProtoType.id;
    this.name = this@toProtoType.name
    this.online = this@toProtoType.online
    this.type = when (this@toProtoType) {
        is Friend -> PacketOuterClass.Contact.ContactType.FRIEND
        is Group -> PacketOuterClass.Contact.ContactType.GROUP
        is Member -> PacketOuterClass.Contact.ContactType.MEMBER
        else -> PacketOuterClass.Contact.ContactType.UNRECOGNIZED
    }
    if (this@toProtoType is Group)
        this.members.addAll(
            this@toProtoType.members.map {
                contact {
                    this.id = it.id
                    this.name = it.name
                    this.online = it.online
                    this.type = PacketOuterClass.Contact.ContactType.MEMBER
                }
            }.toList()
        )
}

internal fun PacketOuterClass.Contact.fromProtoType() = when (this.type) {
    PacketOuterClass.Contact.ContactType.FRIEND -> Friend(
        this.id,
        this.name,
        this.online
    )

    PacketOuterClass.Contact.ContactType.GROUP -> Group(
        this.id,
        this.name,
        this.online,
        this.membersList.map { member ->
            Member(member.id, member.name, member.online)
        }.toList()
    )
    else -> throw IllegalArgumentException("Unknown contact type: ${this.type}")
}