package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.*
import top.focess.netdesign.server.*

data class ContactMessageResponsePacket(val message: Message) : ServerPacket(PACKET_ID) {


    companion object : PacketCompanion<ContactMessageResponsePacket>() {
        override val PACKET_ID: Int = 13

        override fun fromProtoType(packet: Any): ContactMessageResponsePacket {
            val contactMessageResponse: PacketOuterClass.ContactMessageResponse = packet.unpack()
            return ContactMessageResponsePacket(contactMessageResponse.message.fromProtoType())
        }

    }

    override fun toProtoType() = contactMessageResponse {
        this.message = this@ContactMessageResponsePacket.message.toProtoType()
    }

}

data class ContactMessageRequestPacket(val token: String, val id: Int, val internalId: Int) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactMessageRequestPacket>() {
        override val PACKET_ID: Int = 12

        override fun fromProtoType(packet: Any): ContactMessageRequestPacket {
            val contactMessageRequest: PacketOuterClass.ContactMessageRequest = packet.unpack()
            return ContactMessageRequestPacket(contactMessageRequest.token, contactMessageRequest.id, contactMessageRequest.internalId)
        }

    }

    override fun toProtoType() = contactMessageRequest {
        this.token = this@ContactMessageRequestPacket.token
        this.id = this@ContactMessageRequestPacket.id
        this.internalId = this@ContactMessageRequestPacket.internalId
    }
}

internal fun Message.toProtoType() = message {
    this.message = rawMessage {
        this.from = this@toProtoType.from
        this.to = this@toProtoType.to
        this.type = when (this@toProtoType.content.type) {
            MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
            MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
            MessageType.FILE -> PacketOuterClass.MessageType.FILE
        }
        this.content = this@toProtoType.content.content
    }
    this.id = this@toProtoType.id
    this.internalId = this@toProtoType.internalId
    this.timestamp = this@toProtoType.timestamp
}

internal fun PacketOuterClass.Message.fromProtoType() = Message(this.id, this.message.from, this.message.to, this.internalId, when (this.message.type) {
    PacketOuterClass.MessageType.TEXT -> TextMessageContent(this.message.content)
    PacketOuterClass.MessageType.IMAGE -> ImageMessageContent(this.message.content)
    PacketOuterClass.MessageType.FILE -> FileMessageContent(this.message.content)
    else -> throw IllegalArgumentException()
}, this.timestamp)