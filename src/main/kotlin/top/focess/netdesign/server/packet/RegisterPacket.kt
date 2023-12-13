package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass.RegisterResponse
import top.focess.netdesign.proto.PacketOuterClass.RegisterRequest
import top.focess.netdesign.proto.registerRequest
import top.focess.netdesign.proto.registerResponse

data class RegisterRequestPacket(val username: String, val rawPassword: String) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<RegisterRequestPacket>() {
        override val PACKET_ID = 22
        override fun fromProtoType(packet: Any) : RegisterRequestPacket {
            val registerRequest: RegisterRequest = packet.unpack()
            return RegisterRequestPacket(registerRequest.username, registerRequest.rawPassword)
        }
    }

    override fun toProtoType() = registerRequest {
        this.username = this@RegisterRequestPacket.username
        this.rawPassword = this@RegisterRequestPacket.rawPassword
    }
}

data class RegisterResponsePacket(val success: Boolean) : ServerPacket(PACKET_ID) {

    companion object : PacketCompanion<RegisterResponsePacket>() {
        override val PACKET_ID = 23
        override fun fromProtoType(packet: Any): RegisterResponsePacket {
            val registerResponse: RegisterResponse = packet.unpack()
            return RegisterResponsePacket(registerResponse.success)
        }
    }

    override fun toProtoType() = registerResponse {
        this.success = this@RegisterResponsePacket.success
    }
}