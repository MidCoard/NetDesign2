package top.focess.netdesign.server.packet

import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.loginPreRequest
import top.focess.netdesign.proto.loginPreResponse

data class LoginPreResponsePacket(val challenge: String) : ServerPacket(PACKET_ID) {
    companion object {
        val PACKET_ID = 3
    }

    override fun toProtoType(): GeneratedMessageV3 = loginPreResponse {
        this.packetId = PACKET_ID
        this.challenge = this@LoginPreResponsePacket.challenge
    }
}

data class LoginPreRequestPacket(val username: String) : ClientPacket(PACKET_ID) {

    companion object {
        val PACKET_ID = 2
    }

    override fun toProtoType(): GeneratedMessageV3 = loginPreRequest {
        this.packetId = PACKET_ID
        this.username = this@LoginPreRequestPacket.username
    }
}