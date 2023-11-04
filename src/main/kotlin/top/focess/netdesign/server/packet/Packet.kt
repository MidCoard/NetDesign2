package top.focess.netdesign.server.packet

import com.google.protobuf.GeneratedMessageV3

abstract class Packet {
    abstract fun toProtoType() : GeneratedMessageV3
}

abstract class ServerPacket(val packetId: Int) : Packet()

abstract class ClientPacket(val packetId: Int) : Packet()