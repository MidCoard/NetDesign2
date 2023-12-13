package top.focess.netdesign.server.packet

import com.google.protobuf.Any
import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.kotlin.unpack
import top.focess.netdesign.proto.PacketOuterClass.FileUploadRequest
import top.focess.netdesign.proto.PacketOuterClass.FileUploadResponse
import top.focess.netdesign.proto.file
import top.focess.netdesign.proto.fileUploadRequest
import top.focess.netdesign.proto.fileUploadResponse
import top.focess.netdesign.server.File

data class FileUploadRequestPacket(val token: String, val id: String, val file: File) : ClientPacket(PACKET_ID) {

    companion object : PacketCompanion<FileUploadRequestPacket>() {
        override val PACKET_ID = 19
        override fun fromProtoType(packet: Any): FileUploadRequestPacket {
            val fileUploadRequest: FileUploadRequest = packet.unpack()
            return FileUploadRequestPacket(fileUploadRequest.token, fileUploadRequest.id, File(fileUploadRequest.file.name, fileUploadRequest.file.content.toByteArray()))
        }
    }

    override fun toProtoType() = fileUploadRequest {
        this.token = this@FileUploadRequestPacket.token
        this.id = this@FileUploadRequestPacket.id
        this.file = this@FileUploadRequestPacket.file.toProtoType()
    }
}

internal fun File.toProtoType() = file {
    this.name = this@toProtoType.filename
    this.content = this@toProtoType.data.toByteString()
}

data class FileUploadResponsePacket(val success: Boolean) : ServerPacket(PACKET_ID) {
    companion object : PacketCompanion<FileUploadResponsePacket>() {
        override val PACKET_ID = 24
        override fun fromProtoType(packet: Any): FileUploadResponsePacket {
            val fileUploadResponse: FileUploadResponse = packet.unpack()
            return FileUploadResponsePacket(fileUploadResponse.success)
        }
    }

    override fun toProtoType() = fileUploadResponse {
        this.success = this@FileUploadResponsePacket.success
    }
}