package top.focess.netdesign.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.server.packet.ContactMessageRequestPacket
import top.focess.netdesign.server.packet.ContactMessageResponsePacket
import top.focess.netdesign.server.packet.FriendSendMessageRequestPacket
import top.focess.netdesign.server.packet.FriendSendMessageResponsePacket

@Composable
fun LangFile.LangScope.MessageView(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .pointerHoverIcon(PointerIcon.Hand),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar",
            tint = MaterialTheme.colors.primary
        )
        Text(
            text = message.content.toString(),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun LangFile.ColumnLangScope.ChatView(server: RemoteServer, contact: Contact) {

    val messages = mutableStateListOf<Message>()
    var text by remember { mutableStateOf("") }

    var sendRequest by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val localMessages =
            localMessageQueries.selectBySenderAndReceiver(contact.id.toLong(), server.self!!.id.toLong(), 50).executeAsList().map {
            Message(
                it.id.toInt(),
                it.sender.toInt(),
                it.receiver_.toInt(),
                it.internal_id.toInt(),
                when (it.type) {
                    MessageType.TEXT -> TextMessageContent(it.data_)
                    MessageType.IMAGE -> ImageMessageContent(it.data_)
                    MessageType.FILE -> FileMessageContent(it.data_)
                },
                it.timestamp.toInt()
            )
        }.toList()
        var currentInternalId = -1
        for (message in localMessages)
            if (message.internalId > currentInternalId)
                currentInternalId = message.internalId
        messages.addAll(localMessages)
        while (true) {
            val current = System.currentTimeMillis()
            if (sendRequest) {
                val copyText = text;
                text = ""
                val packet = server.sendPacket(FriendSendMessageRequestPacket(server.self!!.id, contact.id, copyText, MessageType.TEXT))
                if (packet is FriendSendMessageResponsePacket) {
                    println("Send Message Success")
                }
                println(packet)
                sendRequest = false
            }
            delay(500 + current - System.currentTimeMillis())
            val packet = server.sendPacket(ContactMessageRequestPacket(contact.id, currentInternalId))
            if (packet is ContactMessageResponsePacket) {
                for (message in packet.messages)
                    if (message.internalId > currentInternalId)
                        currentInternalId = message.internalId
                messages.addAll(packet.messages)
            }
        }
    }


    column {

        LazyColumn(modifier = Modifier.weight(10f)) {
            items(messages.sortedBy { it.internalId }) {
                MessageView(it)
            }
        }

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            singleLine = true,
            placeholder = {
                Text("chat.placeholder".l)
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                if (sendRequest)
                                    return@clickable
                                sendRequest = true
                            }
                            .clip(RoundedCornerShape(5.dp))
                            .padding(10.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colors.primary
                        )
                        Text("chat.send".l)
                    }
                }
            }
        )
    }
}