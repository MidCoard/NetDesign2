package top.focess.netdesign.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.Message

@Composable
fun LangFile.LangScope.ChatView(contact: Contact) {

    var messages = mutableStateListOf<Message>()

    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // get local chat


        while (true) {
            // fetch remote chat

            delay(1000)
        }
    }

    LazyColumn {

    }

    TextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

}