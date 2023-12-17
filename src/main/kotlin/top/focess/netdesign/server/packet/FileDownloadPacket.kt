package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass.FileDownloadRequest
import top.focess.netdesign.proto.fileDownloadRequest
import top.focess.netdesign.server.File

data class FileDownloadRequestPacket(val token: String, val id: String) : ClientPacket(PACKET_ID) {
    companion object : PacketCompanion<FileDownloadRequestPacket>() {
        override val PACKET_ID = 20

        override fun fromProtoType(packet: Any) : FileDownloadRequestPacket {
            val fileDownloadRequest : FileDownloadRequest = packet.unpack()
            return FileDownloadRequestPacket(fileDownloadRequest.token, fileDownloadRequest.id)
        }

    }

    override fun toProtoType() = fileDownloadRequest {
        this.token = this@FileDownloadRequestPacket.token
        this.id = this@FileDownloadRequestPacket.id
    }
}

data class FileDownloadResponsePacket(val file: File) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<FileDownloadResponsePacket>() {
        override val PACKET_ID = 21

        override fun fromProtoType(packet: Any) : FileDownloadResponsePacket {
            val fileDownloadResponse : top.focess.netdesign.proto.PacketOuterClass.FileDownloadResponse = packet.unpack()
            return FileDownloadResponsePacket(fileDownloadResponse.file.fromProtoType())
        }

    }

    override fun toProtoType() = top.focess.netdesign.proto.fileDownloadResponse {
        this.file = this@FileDownloadResponsePacket.file.toProtoType()
    }
}