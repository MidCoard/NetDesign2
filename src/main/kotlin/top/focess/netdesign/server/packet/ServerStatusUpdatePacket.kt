package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass

data class ServerStatusUpdateResponsePacket(val online: Boolean, val registrable: Boolean) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ServerStatusUpdateResponsePacket>() {
        override val PACKET_ID = 7
        override fun fromProtoType(packet: Any): ServerStatusUpdateResponsePacket {
            val serverStatusUpdateResponse: PacketOuterClass.ServerStatusUpdateResponse = packet.unpack()
            return ServerStatusUpdateResponsePacket(serverStatusUpdateResponse.online, serverStatusUpdateResponse.registrable)
        }
    }

    override fun toProtoType() = top.focess.netdesign.proto.serverStatusUpdateResponse {
        this.online = this@ServerStatusUpdateResponsePacket.online
        this.registrable = this@ServerStatusUpdateResponsePacket.registrable
    }
}

class ServerStatusUpdateRequestPacket() : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ServerStatusUpdateRequestPacket>() {
        override val PACKET_ID = 6
        override fun fromProtoType(packet: Any): ServerStatusUpdateRequestPacket {
            return ServerStatusUpdateRequestPacket()
        }
    }

    override fun toProtoType() = top.focess.netdesign.proto.serverStatusUpdateRequest {
    }
}

