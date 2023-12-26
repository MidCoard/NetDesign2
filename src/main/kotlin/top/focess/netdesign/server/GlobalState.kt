package top.focess.netdesign.server

import androidx.compose.runtime.mutableStateListOf

object GlobalState {
    var client : Client = RemoteClient()
    var localServer: LocalServer? = null
    val contacts = mutableStateListOf<Contact>()
}
