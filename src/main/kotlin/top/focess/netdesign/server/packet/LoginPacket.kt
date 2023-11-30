package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.loginRequest
import top.focess.netdesign.proto.loginResponse

data class LoginResponsePacket(val username: String, val logined: Boolean, val token: String) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<LoginResponsePacket>() {
        override val PACKET_ID = 5
        override fun fromProtoType(packet: Any): LoginResponsePacket  {
            val loginResponse: PacketOuterClass.LoginResponse = packet.unpack();
            return LoginResponsePacket(loginResponse.username, loginResponse.logined, loginResponse.token)
        }

    }

    override fun toProtoType(): GeneratedMessageV3 = loginResponse {
        this.logined = this@LoginResponsePacket.logined
    }
}

data class LoginRequestPacket(val username: String, val hashPassword: String) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<LoginRequestPacket>() {
        override val PACKET_ID = 4
        override fun fromProtoType(packet: Any): LoginRequestPacket {
            val loginRequest: PacketOuterClass.LoginRequest = packet.unpack()
            return LoginRequestPacket(loginRequest.username, loginRequest.hashPassword)
        }
    }

    override fun toProtoType(): GeneratedMessageV3 = loginRequest {
        this.username = this@LoginRequestPacket.username
        this.hashPassword = this@LoginRequestPacket.hashPassword
    }

}