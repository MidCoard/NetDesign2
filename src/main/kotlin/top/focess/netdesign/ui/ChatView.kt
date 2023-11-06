package top.focess.netdesign.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.Message

@Composable
fun LangFile.ColumnLangScope.ChatView(contact: Contact) {

    var messages = mutableStateListOf<Message>()

    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // get local chat


        while (true) {
            // fetch remote chat

            delay(10000)
        }
    }

    column {

        LazyColumn(modifier = Modifier.weight(10f)) {

        }

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            singleLine = true,
            trailingIcon = {
                if (text.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clickable {
//                                sendMessage(inputText)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colors.primary
                        )
                        Text("Send")
                    }
                }
            }
        )
    }

}