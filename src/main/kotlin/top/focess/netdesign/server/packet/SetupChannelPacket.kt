package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.setupChannelRequest

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