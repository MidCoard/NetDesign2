package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalState {
    val server = RemoteServer()
    lateinit var singleServer: SingleServer
    val contacts = mutableStateListOf<Contact>()
}
