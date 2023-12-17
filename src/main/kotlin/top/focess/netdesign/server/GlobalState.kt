package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalState {
    val server = RemoteServer()
    var singleServer: SingleServer? = null
    val contacts = mutableStateListOf<Contact>()
}
