package top.focess.netdesign.server

import androidx.compose.runtime.mutableStateListOf

abstract class Contact(val id: Int, val name: String) {

    abstract fun sendMessage(message: Message) : Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}

class Friend(id: Int, name: String) : Contact(id, name) {
    override fun sendMessage(message: Message): Boolean {
        TODO("Not yet implemented")
    }

}

class Member(id: Int, name: String) : Contact(id, name) {
    override fun sendMessage(message: Message) =
        getContact(this.id)?.sendMessage(message) ?: false

}

class Group(id: Int, name: String, members: List<Member>) : Contact(id, name) {

    val members = mutableStateListOf(members)
    override fun sendMessage(message: Message): Boolean {
        TODO("Not yet implemented")
    }
}

val contacts = mutableStateListOf<Contact>()

fun getContact(id: Int) = contacts.find { it.id == id }
