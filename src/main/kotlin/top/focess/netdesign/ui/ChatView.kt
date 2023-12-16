package top.focess.netdesign.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.sqldelight.message.LocalMessage

data class RenderMessage(val messageContent: MessageContent, val contactMessage: Boolean, var internalId: Int = -1, val timestamp: Int)

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
fun LangFile.LangScope.MessageContentView(renderMessage: RenderMessage) {
    when (renderMessage.messageContent.type) {
        MessageType.TEXT -> {
            SelectionContainer {
                Text(
                    text = renderMessage.messageContent.data,
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
fun LangFile.LangScope.MessageView(renderMessage: RenderMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
        ,
        horizontalAlignment = if (renderMessage.contactMessage) Alignment.Start else Alignment.End
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (renderMessage.contactMessage) {
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
                            if (renderMessage.contactMessage) 10.dp else 0.dp,
                            if (renderMessage.contactMessage) 0.dp else 10.dp
                        )
                    )
                    .background(if (renderMessage.contactMessage) OTHER_MESSAGE_COLOR else MY_MESSAGE_COLOR)
                    .padding(
                        start = 10.dp,
                        top = 5.dp,
                        end = 10.dp,
                        bottom = 5.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                MessageContentView(renderMessage)
            }
            if (!renderMessage.contactMessage) {
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

    var messageContent : RawMessageContent? by remember { mutableStateOf(null) }
    var sendRequest by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showDialog = remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(FocessDialog(show = showDialog)) }
    var requestShowLatestMessage by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<RenderMessage>() }

    LaunchedEffect(sendRequest) {
        if (sendRequest) {
            val copyMessageContent = messageContent
            if (copyMessageContent != null) {
                val rawMessageCount = copyMessageContent.messageCount
                val messageList = with(contact) {
                    val list = mutableListOf<Message>()

                    if (copyMessageContent !is RawRichMessageContent) {
                        val textRenderMessage = RenderMessage(
                            copyMessageContent.toMessageContent(),
                            false,
                            -1,
                            System.currentTimeMillis().toInt()
                        )
                        messages.add(textRenderMessage)
                        server.sendMessage(copyMessageContent)?.let {
                            list.add(it)
                            messages.remove(textRenderMessage)
                        }
                    }
                    else {
                        val jobs = mutableListOf<Job>()
                        for (content in copyMessageContent.rawMessageContents)
                            if (content !is RawRichMessageContent) {
                                launch {
                                    val renderMessage = RenderMessage(
                                        content.toMessageContent(),
                                        false,
                                        -1,
                                        System.currentTimeMillis().toInt()
                                    )
                                    server.sendMessage(content)?.let {
                                        list.add(it)
                                        messages.remove(renderMessage)
                                    }
                                }.let {
                                    jobs.add(it)
                                }
                            }
                        jobs.joinAll()
                    }
                    list
                }
                for (message in messageList) {
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
                }

                // indicate send failed
                messages.filter { it.internalId == -1 }.forEach {
                    it.internalId = -2
                }

                if (messageList.size != rawMessageCount) {
                    dialog = FocessDialog(
                        "chat.sendFailed".l,
                        "chat.sendFailedMessage".l,
                        showDialog
                    )
                    dialog.show()
                }
            }
            sendRequest = false
        }
    }

    var lastSize by remember { mutableStateOf(0) }

    LaunchedEffect(contact.messages.size) {
        val newSize = contact.messages.size
        if (newSize > lastSize) {
            for (i in lastSize until newSize) {
                val message = contact.messages[i]
                messages.add(
                    RenderMessage(
                        message.content,
                        message.to == server.self!!.id,
                        message.internalId,
                        message.timestamp
                    )
                )
            }
            lastSize = newSize
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty())
            requestShowLatestMessage = true
    }

    LaunchedEffect(requestShowLatestMessage) {
        if (requestShowLatestMessage) {
            val targetIndex = messages.size - 1
            if (targetIndex >= 0)
                listState.animateScrollToItem(messages.size - 1)
            requestShowLatestMessage = false
        }
    }

    FocessDialogWindow(dialog)

    Box {

        Image(painterResource("imgs/background.jpg"), null, contentScale = ContentScale.Crop)

        Column {

            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                items(messages.sortedBy { it.timestamp }) {
                    MessageView(it)
                }
            }

            InputView(sendRequest, {
                requestShowLatestMessage = true
            }) {
                messageContent = it
                sendRequest = true
            }
        }
    }
}

@Composable
fun DroppedItemView(content: RawMessageContent, onRemove: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
    ) {

        IconButton(
            onClick = { onRemove() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            when (content) {
                is RawImageMessageContent -> {
                    val image = content.image
                    Image(
                        painter = image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showDialog = true }
                    )
                }
                is RawFileMessageContent -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Yellow),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource("icons/description.svg"),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            SelectionContainer {
                                Text(
                                    text = content.file.filename,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (content is RawImageMessageContent) {
                    Image(
                        painter = content.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LangFile.ColumnLangScope.InputView(_sendRequest: Boolean, onDrag: () -> Unit, onMessageSend: (RawMessageContent) -> Unit) {

    var sendRequest by remember { mutableStateOf(_sendRequest) }

    var textMessage by remember { mutableStateOf("") }

    val currentFileMessageContents = remember { mutableStateListOf<RawFileMessageContent>() }
    val needRemoveFileMessageContents = remember { mutableStateListOf<RawFileMessageContent>() }
    var isDroppable by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val resource = painterResource("imgs/background.jpg")

    LaunchedEffect(sendRequest) {
        if (sendRequest) {
            if (currentFileMessageContents.isEmpty())
                onMessageSend(RawTextMessageContent(textMessage))
            else if (textMessage.isEmpty())
                onMessageSend(RawRichMessageContent(*currentFileMessageContents.toTypedArray()))
            else
                onMessageSend(RawRichMessageContent(RawTextMessageContent(textMessage), *currentFileMessageContents.toTypedArray()))
            textMessage = ""
            currentFileMessageContents.clear()
        }
    }

    LaunchedEffect(currentFileMessageContents.size, isDroppable) {
        val targetIndex = if (isDroppable) currentFileMessageContents.size else currentFileMessageContents.size - 1
        if (targetIndex >= 0)
            listState.animateScrollToItem(index = targetIndex)
    }

    LaunchedEffect(needRemoveFileMessageContents.size) {
        if (needRemoveFileMessageContents.isNotEmpty()) {
            currentFileMessageContents.removeAll(needRemoveFileMessageContents)
            needRemoveFileMessageContents.clear()
        }
    }

    fun shouldSend() : Boolean {
        if (textMessage.isNotEmpty())
            return true
        if (currentFileMessageContents.isNotEmpty())
            return true
        return false
    }

        Column(
            modifier = Modifier.wrapContentHeight(Alignment.Bottom).fillMaxWidth()
                .onExternalDrag (
                    onDragStart = {
                        isDroppable = it.dragData is DragData.FilesList || it.dragData is DragData.Image
                        if (isDroppable)
                            onDrag()
                    },
                    onDragExit = {
                        isDroppable = false
                    },
                    onDrop = {
                        isDroppable = false
                        val dragData = it.dragData
//                        if (dragData is DragData.FilesList) {
//                            val uris = dragData.readFiles()
//                            for (uri in uris) {
//                                val file = URI.create(uri).toPath().toFile()
//                                if (!file.exists() || file.isDirectory)
//                                    continue
//                                currentFileMessageContents.add(RawFileMessageContent(File(file.name, Files.readAllBytes(file.toPath()))))
//                            }
//                        } else if (dragData is DragData.Image)
//                            currentFileMessageContents.add(RawImageMessageContent(dragData.readImage()))
                        currentFileMessageContents.add(RawImageMessageContent(resource))
                    }

                )
        ) {

            if (currentFileMessageContents.isNotEmpty() || isDroppable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                        .background(Color.LightGray.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                ) {
                    LazyRow(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState
                    ) {
                        items(currentFileMessageContents) { content ->
                            DroppedItemView(content) {
                                needRemoveFileMessageContents.add(content)
                            }
                        }
                        if (isDroppable) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }

                TextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    modifier = Modifier.fillMaxWidth().onKeyEvent {
                        if (it.key == Key.Enter) {
                            if (sendRequest)
                                return@onKeyEvent true
                            if (shouldSend())
                                sendRequest = true
                        }
                        it.key == Key.Enter
                    },
                    singleLine = true,
                    placeholder = {
                        Text("chat.placeholder".l)
                    },
                    trailingIcon = {
                        if (shouldSend()) {
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
                                    contentDescription = "chat.send".l,
                                    tint = MaterialTheme.colors.primary
                                )
                                Text("chat.send".l)
                            }
                        }
                    }
                )
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