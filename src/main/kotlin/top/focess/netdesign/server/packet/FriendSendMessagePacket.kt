package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.*
import top.focess.netdesign.server.*

data class FriendSendMessageResponsePacket(val message: Message) : ServerPacket(PACKET_ID)  {

    companion object : PacketCompanion<FriendSendMessageResponsePacket>() {
        override val PACKET_ID = 15

        override fun fromProtoType(packet: Any): FriendSendMessageResponsePacket {
            val friendSendMessageResponse: PacketOuterClass.FriendSendMessageResponse = packet.unpack()
            val message = friendSendMessageResponse.message
            return FriendSendMessageResponsePacket(
                Message(
                    message.id,
                    message.message.from,
                    message.message.to,
                    message.internalId,
                    when(message.message.type) {
                        PacketOuterClass.MessageType.TEXT -> top.focess.netdesign.server.TextMessageContent(message.message.content)
                        PacketOuterClass.MessageType.IMAGE -> top.focess.netdesign.server.ImageMessageContent(message.message.content)
                        PacketOuterClass.MessageType.FILE -> top.focess.netdesign.server.FileMessageContent(message.message.content)
                        else -> throw IllegalArgumentException("Unknown message type")
                    },
                    message.timestamp
                )
            )
        }

    }

    override fun toProtoType() = friendSendMessageResponse {
        this.message = message {
            this.message = rawMessage {
                this.from = this@FriendSendMessageResponsePacket.message.from
                this.to = this@FriendSendMessageResponsePacket.message.to
                this.type = when(this@FriendSendMessageResponsePacket.message.content.type) {
                    MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
                    MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
                    MessageType.FILE -> PacketOuterClass.MessageType.FILE
                }
                this.content = this@FriendSendMessageResponsePacket.message.content.data
            }
            this.id = this@FriendSendMessageResponsePacket.message.id
            this.internalId = this@FriendSendMessageResponsePacket.message.internalId
            this.timestamp = this@FriendSendMessageResponsePacket.message.timestamp
        }
    }
}

data class FriendSendMessageRequestPacket(val token: String, val from: Int, val to: Int, val messageContent: MessageContent) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<FriendSendMessageRequestPacket>() {
        override val PACKET_ID = 14

        override fun fromProtoType(packet: Any): FriendSendMessageRequestPacket {
            val friendSendMessageRequest: PacketOuterClass.FriendSendMessageRequest = packet.unpack()
            val message = friendSendMessageRequest.message
            return FriendSendMessageRequestPacket(
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

    override fun toProtoType() = friendSendMessageRequest {
        this.token = this@FriendSendMessageRequestPacket.token
        this.message = rawMessage {
            this.from = this@FriendSendMessageRequestPacket.from
            this.to = this@FriendSendMessageRequestPacket.to
            this.content = this@FriendSendMessageRequestPacket.messageContent.data
            this.type = when(this@FriendSendMessageRequestPacket.messageContent.type) {
                MessageType.TEXT -> PacketOuterClass.MessageType.TEXT
                MessageType.IMAGE -> PacketOuterClass.MessageType.IMAGE
                MessageType.FILE -> PacketOuterClass.MessageType.FILE
            }

        }
    }
}