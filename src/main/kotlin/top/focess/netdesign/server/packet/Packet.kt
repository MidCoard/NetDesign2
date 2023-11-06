package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.packet

abstract class Packet(val packetId: Int) {
    protected abstract fun toProtoType() : GeneratedMessageV3

    fun toProtoPacket() = packet {
        this.packetId = this@Packet.packetId
        this.content = Any.pack(toProtoType())
    }
}

abstract class ServerPacket(packetId: Int) : Packet(packetId)

abstract class ClientPacket(packetId: Int) : Packet(packetId)