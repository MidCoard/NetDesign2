package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.serverStatusRequest
import top.focess.netdesign.proto.serverStatusResponse

data class ServerStatusResponsePacket(val online: Boolean, val registrable: Boolean, val serverPublicKey: String?) : ServerPacket(PACKET_ID) {

    companion object : PacketCompanion<ServerStatusResponsePacket>() {
        override val PACKET_ID = 1
        override fun fromProtoType(packet: Any) : ServerStatusResponsePacket {
            val serverStatusResponse: PacketOuterClass.ServerStatusResponse = packet.unpack()
            return ServerStatusResponsePacket(serverStatusResponse.online, serverStatusResponse.registrable, serverStatusResponse.serverPublicKey)
        }
    }

    override fun toProtoType() = serverStatusResponse {
        this.online = this@ServerStatusResponsePacket.online
        this.registrable = this@ServerStatusResponsePacket.registrable
        this.serverPublicKey = this@ServerStatusResponsePacket.serverPublicKey
    }
}

data class ServerStatusRequestPacket(val clientPublicKey: String?) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<ServerStatusRequestPacket>() {
        override val PACKET_ID = 0
        override fun fromProtoType(packet: Any) : ServerStatusRequestPacket {
            val serverStatusRequest: PacketOuterClass.ServerStatusRequest = packet.unpack()
            return ServerStatusRequestPacket(serverStatusRequest.clientPublicKey)
        }
    }

    override fun toProtoType() = serverStatusRequest {
        this.clientPublicKey = this@ServerStatusRequestPacket.clientPublicKey
    }

}

