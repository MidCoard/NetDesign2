package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass.DeleteMessageRequest
import top.focess.netdesign.proto.PacketOuterClass.DeleteMessageResponse
import top.focess.netdesign.proto.deleteMessageRequest
import top.focess.netdesign.proto.deleteMessageResponse

data class DeleteMessageRequestPacket(val token: String, val id: Int) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<DeleteMessageRequestPacket>() {
        override val PACKET_ID = 25

        override fun fromProtoType(packet: Any) : DeleteMessageRequestPacket {
            val deleteMessageRequest: DeleteMessageRequest = packet.unpack()
            return DeleteMessageRequestPacket(deleteMessageRequest.token, deleteMessageRequest.id)
        }

    }

    override fun toProtoType() = deleteMessageRequest {
        this.token = this@DeleteMessageRequestPacket.token
        this.id = this@DeleteMessageRequestPacket.id
    }
}

data class DeleteMessageResponsePacket(val success: Boolean) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<DeleteMessageResponsePacket>() {
        override val PACKET_ID = 26

        override fun fromProtoType(packet: Any): DeleteMessageResponsePacket {
            val deleteMessageResponse : DeleteMessageResponse = packet.unpack()
            return DeleteMessageResponsePacket(deleteMessageResponse.success)
        }

    }

    override fun toProtoType() = deleteMessageResponse {
        this.success = this@DeleteMessageResponsePacket.success
    }
}