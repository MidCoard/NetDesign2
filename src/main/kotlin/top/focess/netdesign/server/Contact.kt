package top.focess.netdesign.server

import androidx.compose.runtime.*

abstract class Contact(id: Int, name: String, online: Boolean) {

    val id by mutableStateOf(id)
    var name by mutableStateOf(name)
    var online by mutableStateOf(online)

    abstract fun sendMessage(message: Message) : Boolean

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
    override fun sendMessage(message: Message): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Friend(id=$id, name='$name', online=$online)"
    }

}

class Member(id: Int, name: String, online: Boolean) : Contact(id, name, online) {
    override fun sendMessage(message: Message) =
        getContact(this.id)?.sendMessage(message) ?: false

    override fun toString(): String {
        return "Member(id=$id, name='$name', online=$online)"
    }

}

class Group(id: Int, name: String, online: Boolean, members: List<Member>) : Contact(id, name, online) {

    val members = members.toMutableStateList()

    override fun sendMessage(message: Message): Boolean {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Group(id=$id, name='$name', online=$online, members=$members)"
    }
}

val contacts = mutableStateListOf<Contact>()

fun getContact(id: Int) = contacts.find { it.id == id }
