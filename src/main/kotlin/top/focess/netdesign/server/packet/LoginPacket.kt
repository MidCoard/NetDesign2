package top.focess.netdesign.server.packet

import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.loginRequest
import top.focess.netdesign.proto.loginResponse

data class LoginResponsePacket(val logined: Boolean) : ServerPacket(PACKET_ID) {
    companion object {
        val PACKET_ID = 5
    }

    override fun toProtoType(): GeneratedMessageV3 = loginResponse {
        this.logined = this@LoginResponsePacket.logined
    }
}

data class LoginRequestPacket(val username: String, val hashPassword: String) : ClientPacket(PACKET_ID) {

    companion object {
        val PACKET_ID = 4
    }

    override fun toProtoType(): GeneratedMessageV3 = loginRequest {
        this.username = this@LoginRequestPacket.username
        this.hashPassword = this@LoginRequestPacket.hashPassword
    }

}