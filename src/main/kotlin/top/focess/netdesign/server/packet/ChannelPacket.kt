package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.*

class SetupChannelRequestPacket(val token: String) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<SetupChannelRequestPacket>() {
        override val PACKET_ID = 16
        override fun fromProtoType(packet: Any): SetupChannelRequestPacket {
            val setupChannelRequest : PacketOuterClass.SetupChannelRequest = packet.unpack()
            return SetupChannelRequestPacket(setupChannelRequest.token)

        }
    }

    override fun toProtoType() = setupChannelRequest {
        this.token = this@SetupChannelRequestPacket.token
    }
}

class ClientAckResponsePacket : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ClientAckResponsePacket>() {
        override val PACKET_ID = 9
        override fun fromProtoType(packet: Any) = ClientAckResponsePacket()
    }

    override fun toProtoType() = clientAckResponse { }
}

class ServerAckResponsePacket : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ServerAckResponsePacket>() {
        override val PACKET_ID = 11
        override fun fromProtoType(packet: Any) = ServerAckResponsePacket()
    }

    override fun toProtoType() = serverAckResponse { }
}

class ChannelHeartRequestPacket : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<ChannelHeartRequestPacket>() {
        override val PACKET_ID = 17
        override fun fromProtoType(packet: Any) = ChannelHeartRequestPacket()
    }

    override fun toProtoType() = channelHeartRequest { }
}