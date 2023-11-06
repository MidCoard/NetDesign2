package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.*
import top.focess.netdesign.server.*

data class ContactMessageResponsePacket(val messages: List<Message>) : ServerPacket(PACKET_ID) {


    companion object : PacketCompanion<ContactMessageResponsePacket>() {
        override val PACKET_ID: Int = 13

        override fun fromProtoType(packet: Any): ContactMessageResponsePacket {
            val contactMessageResponse: PacketOuterClass.ContactMessageResponse = packet.unpack()
            return ContactMessageResponsePacket(contactMessageResponse.messagesList.map {
                Message(it.id, it.message.from, it.message.to, it.internalId, when (it.message.type) {
                    PacketOuterClass.MessageType.TEXT -> TextMessageContent(it.message.content)
                    PacketOuterClass.MessageType.IMAGE -> ImageMessageContent(it.message.content)
                    PacketOuterClass.MessageType.FILE -> FileMessageContent(it.message.content)
                    else -> throw IllegalArgumentException()
                }, it.timestamp)
            }.toList())
        }

    }

    override fun toProtoType() = contactMessageResponse {
        this.messages.addAll(this@ContactMessageResponsePacket.messages.map {
            message {
                this.message = rawMessage {
                    this.from = it.from
                    this.to = it.to
                    this.type = when (it.content.type) {
                        MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
                        MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
                        MessageType.FILE -> PacketOuterClass.MessageType.FILE
                    }
                    this.content = it.content.data
                }
                this.id = it.id
                this.internalId = it.internalId
                this.timestamp = it.timestamp
            }
        }.toList())
    }

}

data class ContactMessageRequestPacket(val id: Int, val internalId: Int) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ContactMessageRequestPacket>() {
        override val PACKET_ID: Int = 12

        override fun fromProtoType(packet: Any): ContactMessageRequestPacket {
            val contactMessageRequest: PacketOuterClass.ContactMessageRequest = packet.unpack()
            return ContactMessageRequestPacket(contactMessageRequest.id, contactMessageRequest.internalId)
        }

    }

    override fun toProtoType() = contactMessageRequest {
        this.id = this@ContactMessageRequestPacket.id
        this.internalId = this@ContactMessageRequestPacket.internalId
    }
}