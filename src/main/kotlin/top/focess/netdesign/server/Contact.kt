package top.focess.netdesign.server

import androidx.compose.runtime.*
import top.focess.netdesign.server.GlobalState.contacts
import top.focess.netdesign.server.packet.*

abstract class Contact(val id: Int,val name: String, online: Boolean) {

    var online by mutableStateOf(online)

    val messages = mutableStateListOf<Message>()
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
        val packet = this.sendPacket(SendMessageRequestPacket(this.token!!, this.id!!, this@Friend.id, message.toMessageContent()))
        if (packet is SendMessageResponsePacket) {
            if (packet.message.id == -1)
                return null
            // special case: if message type is file or image
            if (message is RawFileMessageContent) {
                val fileId = packet.message.content.content
                val filePacket = this.sendPacket(FileUploadRequestPacket(this.token!!, fileId, message.file, message.file.sha256()))
                if (filePacket !is FileUploadResponsePacket || !filePacket.success) {
                    // immediately delete the message
                    this.sendPacket(DeleteMessageRequestPacket(this.token!!, packet.message.id))
                    // ignore the response because it should not happen here that upload failed
                    return null
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

fun getContact(id: Int) : Contact? = contacts.find { it.id == id }
