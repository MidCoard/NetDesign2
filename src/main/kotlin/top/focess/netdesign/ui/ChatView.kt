package top.focess.netdesign.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.sqldelight.message.LocalMessage
// wechat-like green color
val MY_MESSAGE_COLOR = Color(0xFFC5E1A5)
// wechat-like blue color
val OTHER_MESSAGE_COLOR = Color(0xFFBBDEFB)

// Adapted from https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class TriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = if(risingToTheRight) {
            Path().apply {
                moveTo(x = 0f, y = size.height)
                lineTo(x = size.width, y = 0f)
                lineTo(x = size.width, y = size.height)
            }
        } else {
            Path().apply {
                moveTo(x = 0f, y = 0f)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)
            }
        }

        return Outline.Generic(path = trianglePath)
    }
}

@Composable
fun Triangle(risingToTheRight: Boolean, background: Color) {
    Box(
        Modifier
            .padding(bottom = 10.dp)
            .clip(TriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(6.dp)
    )
}

@Composable
fun LangFile.LangScope.MessageContentView(messageContent: MessageContent) {
    when (messageContent.type) {
        MessageType.TEXT -> {
            SelectionContainer {
                Text(
                    text = messageContent.data,
                    style = MaterialTheme.typography.body1
                )
            }
        }

        MessageType.FILE -> {

        }

        MessageType.IMAGE -> {

        }
    }
}

@Composable
fun LangFile.LangScope.MessageView(message: Message, contactMessage: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
        ,
        horizontalAlignment = if (contactMessage) Alignment.Start else Alignment.End
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (contactMessage) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colors.primary
                    )
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    Triangle(true, OTHER_MESSAGE_COLOR)
                }
            }
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            10.dp, 10.dp,
                            if (contactMessage) 10.dp else 0.dp,
                            if (contactMessage) 0.dp else 10.dp
                        )
                    )
                    .background(if (contactMessage) OTHER_MESSAGE_COLOR else MY_MESSAGE_COLOR)
                    .padding(
                        start = 10.dp,
                        top = 5.dp,
                        end = 10.dp,
                        bottom = 5.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                MessageContentView(message.content)
            }
            if (!contactMessage) {
                Column {
                    Triangle(false, MY_MESSAGE_COLOR)
                }
            }
        }
    }

    Spacer(Modifier.size(10.dp))
}

@Composable
fun LangFile.ColumnLangScope.ChatView(server: RemoteServer, contact: Contact) {

    var text by remember { mutableStateOf("") }
    var sendRequest by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showDialog = remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(FocessDialog(show = showDialog)) }

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
                contact.messages.add(message)
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

    LaunchedEffect(contact.messages.size) {
        if (contact.messages.isNotEmpty())
            listState.animateScrollToItem(contact.messages.size - 1)
    }

    FocessDialogWindow(dialog)

    Box {

        Image(painterResource("imgs/background.jpg"), null, contentScale = ContentScale.Crop)

        Column {

            LazyColumn(state = listState, modifier = Modifier.weight(10f)) {
                items(contact.messages.sortedBy { it.internalId }) {
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