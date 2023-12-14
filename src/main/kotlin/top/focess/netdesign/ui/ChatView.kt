package top.focess.netdesign.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.server.packet.ContactMessageRequestPacket
import top.focess.netdesign.server.packet.ContactMessageResponsePacket
import top.focess.netdesign.server.packet.FriendSendMessageRequestPacket
import top.focess.netdesign.server.packet.FriendSendMessageResponsePacket
import top.focess.netdesign.sqldelight.message.LocalMessage

@Composable
fun LangFile.LangScope.MessageContentView(messageContent: MessageContent) {
    when (messageContent.type) {
        MessageType.TEXT -> {
            Text(
                text = messageContent.data,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(end = 10.dp)
            )
        }

        MessageType.FILE -> {

        }

        MessageType.IMAGE -> {

        }
    }
}

@Composable
fun LangFile.LangScope.MessageView(message: Message, renderLeft: Boolean) {
    if (renderLeft)
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
            MessageContentView(message.content)
        }
    else
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .pointerHoverIcon(PointerIcon.Hand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            MessageContentView(message.content)
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colors.primary
            )
        }
}

@Composable
fun LangFile.ColumnLangScope.ChatView(server: RemoteServer, contact: Contact) {

    val messages = remember { mutableStateListOf<Message>() }
    var text by remember { mutableStateOf("") }
    var sendRequest by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val showDialog = remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(FocessDialog(show = showDialog)) }

    LaunchedEffect(Unit) {
        val localMessages = queryLatestLocalMessages(contact.id.toLong(), server.id!!.toLong())

        messages.addAll(localMessages)

        val currentInternalId : Int = localMessages.maxOfOrNull { it.internalId } ?: 0

        println("currentInternalId: $currentInternalId")

        val missingInternalIds = (1..currentInternalId).toSet().subtract(localMessages.map { it.internalId }.toSet())
        println("missing messages count: ${missingInternalIds.size}")

        for (missingInternalId in missingInternalIds) {
            val packet = server.sendPacket(ContactMessageRequestPacket(server.token!!, contact.id, missingInternalId))
            if (packet is ContactMessageResponsePacket) {
                val message = packet.message
                if (message.id != -1)
                    messages.add(message)
            }
        }

        // find possible messages from currentInternalId
        var current = currentInternalId + 1;

        while (true) {
            val packet = server.sendPacket(ContactMessageRequestPacket(server.token!!, contact.id, current))
            if (packet is ContactMessageResponsePacket) {
                val message = packet.message
                if (message.id != -1) {
                    messages.add(message)
                    current++
                    continue
                }
            }
            break
        }

        loading = false
    }

    LaunchedEffect(sendRequest) {
        if (sendRequest) {
            val copyText = text;
            text = ""
            val message = with(contact) {
                server.sendMessage(RawTextMessageContent(copyText))
            }
            if (message != null) {
                localMessageQueries.insert(
                    message.id.toLong(),
                    message.from.toLong(),
                    message.to.toLong(),
                    message.content.data,
                    message.content.type,
                    message.timestamp.toLong(),
                    message.internalId.toLong(),
                )
                messages.add(message)
            } else {
                dialog = FocessDialog(
                    "chat.sendFailed".l,
                    "chat.sendFailedMessage".l,
                    showDialog
                )
                dialog.show()
            }
            sendRequest = false
        }
    }

    FocessDialogWindow(dialog)

    Box {

        Image(painterResource("imgs/background.jpg"), null, contentScale = ContentScale.Crop)

        Column {

            LazyColumn(modifier = Modifier.weight(10f)) {
                items(messages.sortedBy { it.internalId }) {
                    MessageView(it, it.from == contact.id)
                }
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().weight(1f).onKeyEvent {
                    if (it.key == Key.Enter) {
                        sendRequest = true
                    }
                    it.key == Key.Enter
                },
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
}

internal fun LocalMessage.toMessage() : Message = Message(
    this.id.toInt(),
    this.sender.toInt(),
    this.receiver_.toInt(),
    this.internal_id.toInt(),
    when (this.type) {
        MessageType.TEXT -> TextMessageContent(this.data_)
        MessageType.IMAGE -> ImageMessageContent(this.data_)
        MessageType.FILE -> FileMessageContent(this.data_)
    },
    this.timestamp.toInt()
)