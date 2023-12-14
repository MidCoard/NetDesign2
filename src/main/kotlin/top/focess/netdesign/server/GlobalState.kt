package top.focess.netdesign.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


val server = RemoteServer()
val singleServer = SingleServer("Local")
val contacts = mutableStateListOf<Contact>()