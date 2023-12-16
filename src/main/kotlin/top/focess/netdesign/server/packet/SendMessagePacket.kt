package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.*
import top.focess.netdesign.server.*

data class SendMessageResponsePacket(val message: Message) : ServerPacket(PACKET_ID)  {

    companion object : PacketCompanion<SendMessageResponsePacket>() {
        override val PACKET_ID = 15

        override fun fromProtoType(packet: Any): SendMessageResponsePacket {
            val friendSendMessageResponse: PacketOuterClass.SendMessageResponse = packet.unpack()
            val message = friendSendMessageResponse.message
            return SendMessageResponsePacket(
                Message(
                    message.id,
                    message.message.from,
                    message.message.to,
                    message.internalId,
                    when(message.message.type) {
                        PacketOuterClass.MessageType.TEXT -> TextMessageContent(message.message.content)
                        PacketOuterClass.MessageType.IMAGE -> ImageMessageContent(message.message.content)
                        PacketOuterClass.MessageType.FILE -> FileMessageContent(message.message.content)
                        else -> throw IllegalArgumentException("Unknown message type")
                    },
                    message.timestamp
                )
            )
        }

    }

    override fun toProtoType() = sendMessageResponse {
        this.message = message {
            this.message = rawMessage {
                this.from = this@SendMessageResponsePacket.message.from
                this.to = this@SendMessageResponsePacket.message.to
                this.type = when(this@SendMessageResponsePacket.message.content.type) {
                    MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
                    MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
                    MessageType.FILE -> PacketOuterClass.MessageType.FILE
                }
                this.content = this@SendMessageResponsePacket.message.content.data
            }
            this.id = this@SendMessageResponsePacket.message.id
            this.internalId = this@SendMessageResponsePacket.message.internalId
            this.timestamp = this@SendMessageResponsePacket.message.timestamp
        }
    }
}

data class SendMessageRequestPacket(val token: String, val from: Int, val to: Int, val messageContent: MessageContent) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<SendMessageRequestPacket>() {
        override val PACKET_ID = 14

        override fun fromProtoType(packet: Any): SendMessageRequestPacket {
            val friendSendMessageRequest: PacketOuterClass.SendMessageRequest = packet.unpack()
            val message = friendSendMessageRequest.message
            return SendMessageRequestPacket(
                friendSendMessageRequest.token,
                message.from,
                message.to,
                when(message.type) {
                    PacketOuterClass.MessageType.TEXT -> TextMessageContent(message.content)
                    PacketOuterClass.MessageType.IMAGE -> ImageMessageContent(message.content)
                    PacketOuterClass.MessageType.FILE -> FileMessageContent(message.content)
                    else -> throw IllegalArgumentException("Unknown message type")
                }
            )
        }

    }

    override fun toProtoType() = sendMessageRequest {
        this.token = this@SendMessageRequestPacket.token
        this.message = rawMessage {
            this.from = this@SendMessageRequestPacket.from
            this.to = this@SendMessageRequestPacket.to
            this.content = this@SendMessageRequestPacket.messageContent.data
            this.type = when(this@SendMessageRequestPacket.messageContent.type) {
                MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
                MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
                MessageType.FILE -> PacketOuterClass.MessageType.FILE
            }

        }
    }
}