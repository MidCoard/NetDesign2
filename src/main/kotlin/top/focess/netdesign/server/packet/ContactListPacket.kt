package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.contactListRequest
import top.focess.netdesign.server.Contact

data class ContactListRequestPacket(val contacts: List<Contact>, val internalIds : List<Int>) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactListRequestPacket>() {
        override val PACKET_ID = 8

        override fun fromProtoType(packet: Any): ContactListRequestPacket {
            val contactListResponse: PacketOuterClass.ContactListRequest = packet.unpack()
            return ContactListRequestPacket(contactListResponse.contactsList.map { contact -> contact.fromProtoType() }, contactListResponse.internalIdsList)
        }

    }

    override fun toProtoType(): GeneratedMessageV3 = contactListRequest {
        this.contacts.addAll(this@ContactListRequestPacket.contacts.map { contact -> contact.toProtoType() })
        this.internalIds.addAll(this@ContactListRequestPacket.internalIds)
    }
}