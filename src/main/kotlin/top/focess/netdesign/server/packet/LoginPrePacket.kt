package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.loginPreRequest
import top.focess.netdesign.proto.loginPreResponse

data class LoginPreResponsePacket(val challenge: String) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<LoginPreResponsePacket>() {
        override val PACKET_ID = 3
        override fun fromProtoType(packet: Any): LoginPreResponsePacket {
            val loginPreResponse: PacketOuterClass.LoginPreResponse = packet.unpack()
            return LoginPreResponsePacket(loginPreResponse.challenge)
        }
    }

    override fun toProtoType(): GeneratedMessageV3 = loginPreResponse {
        this.challenge = this@LoginPreResponsePacket.challenge
    }
}

data class LoginPreRequestPacket(val username: String) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<LoginPreRequestPacket>() {
        override val PACKET_ID = 2
        override fun fromProtoType(packet: Any): LoginPreRequestPacket {
            val loginPreRequest: PacketOuterClass.LoginPreRequest = packet.unpack()
            return LoginPreRequestPacket(loginPreRequest.username)
        }
    }

    override fun toProtoType(): GeneratedMessageV3 = loginPreRequest {
        this.username = this@LoginPreRequestPacket.username
    }
}