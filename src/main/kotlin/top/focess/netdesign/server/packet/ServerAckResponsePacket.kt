package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import top.focess.netdesign.proto.serverAckResponse

class ServerAckResponse : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ServerAckResponse>() {
        override val PACKET_ID = 11
        override fun fromProtoType(packet: Any) = ServerAckResponse()
    }

    override fun toProtoType() = serverAckResponse {  }
}