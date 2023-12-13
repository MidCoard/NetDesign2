package top.focess.netdesign.server

import androidx.compose.runtime.*
import top.focess.netdesign.server.packet.FileUploadRequestPacket
import top.focess.netdesign.server.packet.FileUploadResponsePacket
import top.focess.netdesign.server.packet.FriendSendMessageRequestPacket
import top.focess.netdesign.server.packet.FriendSendMessageResponsePacket

abstract class Contact(id: Int, name: String, online: Boolean) {

    val id by mutableStateOf(id)
    var name by mutableStateOf(name)
    var online by mutableStateOf(online)

     abstract suspend fun RemoteServer.sendMessage(message: RawMessageContent) : Message?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "Contact(id=$id, name='$name', online=$online)"
    }
}

class Friend(id: Int, name: String, online: Boolean) : Contact(id, name, online) {
    override suspend fun RemoteServer.sendMessage(message: RawMessageContent): Message? {
        val packet = this.sendPacket(FriendSendMessageRequestPacket(this.token!!, this.id!!, id!!, message.toMessageContent()))
        if (packet is FriendSendMessageResponsePacket) {
            if (packet.message.id == -1)
                return null
            // special case: if message type is file or image
            if (message is ARawFileMessageContent) {
                val fileId = packet.message.content.data
                val filePacket = this.sendPacket(FileUploadRequestPacket(this.token!!, fileId, message.file))
                // todo handle file upload case
                if (filePacket is FileUploadResponsePacket && filePacket.success) {
                    // success case
                } else {
                    // fail case
                }
            }
            return packet.message
        }
        return null
    }

    override fun toString(): String {
        return "Friend(id=$id, name='$name', online=$online)"
    }

}

class Member(id: Int, name: String, online: Boolean) : Contact(id, name, online) {
    override suspend fun RemoteServer.sendMessage(message: RawMessageContent) : Message? {
        return with(getContact(id!!)) {
            sendMessage(message) // todo problem?
        }
    }

    override fun toString(): String {
        return "Member(id=$id, name='$name', online=$online)"
    }

}

class Group(id: Int, name: String, online: Boolean, members: List<Member>) : Contact(id, name, online) {

    val members = members.toMutableStateList()

    override suspend fun RemoteServer.sendMessage(message: RawMessageContent): Message? {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Group(id=$id, name='$name', online=$online, members=$members)"
    }
}

val contacts = mutableStateListOf<Contact>()

fun getContact(id: Int) : Contact? = contacts.find { it.id == id }
