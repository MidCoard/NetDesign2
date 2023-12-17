package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.GeneratedMessageV3
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.packet

abstract class Packet(val packetId: Int) {
    protected abstract fun toProtoType(): GeneratedMessageV3
    fun toProtoPacket() = packet {
        this.packetId = this@Packet.packetId
        this.content = Any.pack(toProtoType())
    }
}

object Packets {
    private val packets = mutableMapOf<Int, PacketCompanion<*>>()
    private fun registerPacket(packetCompanion: PacketCompanion<*>) {
        packets[packetCompanion.PACKET_ID] = packetCompanion
    }

    init {
        registerPacket(ServerStatusRequestPacket)
        registerPacket(ServerStatusResponsePacket)
        registerPacket(ServerStatusUpdateRequestPacket)
        registerPacket(ServerStatusUpdateResponsePacket)
        registerPacket(LoginPreRequestPacket)
        registerPacket(LoginPreResponsePacket)
        registerPacket(LoginRequestPacket)
        registerPacket(LoginResponsePacket)
        registerPacket(ContactRequestPacket)
        registerPacket(ContactListRequestPacket)
        registerPacket(ContactMessageRequestPacket)
        registerPacket(ContactMessageResponsePacket)
        registerPacket(SendMessageRequestPacket)
        registerPacket(SendMessageResponsePacket)
        registerPacket(SetupChannelRequestPacket)
        registerPacket(RegisterRequestPacket)
        registerPacket(RegisterResponsePacket)
        registerPacket(ServerAckResponsePacket)
        registerPacket(ClientAckResponsePacket)
        registerPacket(ChannelHeartRequestPacket)
        registerPacket(ContactMessageListRequestPacket)
        registerPacket(DeleteMessageRequestPacket)
        registerPacket(DeleteMessageResponsePacket)
        registerPacket(FileDownloadRequestPacket)
        registerPacket(FileDownloadResponsePacket)
    }

    fun fromProtoPacket(packet: PacketOuterClass.Packet) = packets[packet.packetId]?.fromProtoType(packet.content)
        ?: throw IllegalArgumentException("Unknown packet id: ${packet.packetId}")
}

abstract class PacketCompanion<V : Packet> {

    abstract val PACKET_ID: Int
    abstract fun fromProtoType(packet: Any): V

}

abstract class ServerPacket(packetId: Int) : Packet(packetId)

abstract class ClientPacket(packetId: Int) : Packet(packetId)