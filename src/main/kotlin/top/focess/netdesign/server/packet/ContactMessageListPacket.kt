package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass.ContactMessageListRequest
import top.focess.netdesign.proto.contactMessageListRequest
import top.focess.netdesign.server.Message

data class ContactMessageListRequestPacket(val messages: List<Message>) : ServerPacket(PACKET_ID) {

    constructor(vararg messages: Message) : this(messages.toList())
    companion object : PacketCompanion<ContactMessageListRequestPacket>() {
        override val PACKET_ID = 18

        override fun fromProtoType(packet: Any) : ContactMessageListRequestPacket {
            val contactMessageListRequest : ContactMessageListRequest = packet.unpack()
            return ContactMessageListRequestPacket(contactMessageListRequest.messagesList.map { it.fromProtoType() })
        }

    }

    override fun toProtoType() = contactMessageListRequest {
        this.messages.addAll(this@ContactMessageListRequestPacket.messages.map { it.toProtoType() })
    }
}
