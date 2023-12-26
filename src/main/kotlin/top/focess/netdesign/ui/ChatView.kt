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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
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
import kotlinx.coroutines.*
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.server.GlobalState.client
import top.focess.netdesign.server.packet.FileDownloadRequestPacket
import top.focess.netdesign.server.packet.FileDownloadResponsePacket
import top.focess.netdesign.sqldelight.message.LocalMessage
import java.net.URI
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.toPath

class  RenderMessage(
    _messageContent: MessageContent,
    val contactMessage: Boolean,
    val timestamp: Int,
    _internalId: Int = -1
) {
    var internalId by mutableStateOf(_internalId)
    val type = _messageContent.type
    var content by mutableStateOf(_messageContent.content)
}

// wechat-like green color
val MY_MESSAGE_COLOR = Color(0xFFC5E1A5)

// wechat-like blue color
val OTHER_MESSAGE_COLOR = Color(0xFFBBDEFB)

val fileState = FileState()

// Adapted from https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class TriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val trianglePath = if (risingToTheRight) {
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
        Modifier.padding(bottom = 10.dp).clip(TriangleEdgeShape(risingToTheRight)).background(background).size(6.dp)
    )
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun LangFile.RowLangScope.MessageContentView(renderMessage: RenderMessage) {

    var fileLoading by remember { mutableStateOf(true) }
    var file by remember { mutableStateOf(EMPTY_FILE) }
    var imageBitmap: ImageBitmap? by remember { mutableStateOf(null) }
    var showDialog by remember { mutableStateOf(false) }
    var requestDownload by remember { mutableStateOf(false) }

    LaunchedEffect(requestDownload) {
        if (requestDownload) {
            val requestFile = fileState.result(file.filename)
            if (requestFile != null)
                Files.write(requestFile.toPath(), file.data)
            requestDownload = false
        }
    }

    DisposableEffect(renderMessage.internalId) {
        GlobalScope.launch {
            if ((renderMessage.type == MessageType.FILE || renderMessage.type == MessageType.IMAGE) && fileLoading) {
                val data = renderMessage.content
                if (data.isNotEmpty()) {
                    if (client.id != 0) {
                        val localFile = localFileQueries.select(data).executeAsOneOrNull()
                        if (localFile != null) {
                            file = localFile.toFile()
                            if (renderMessage.type == MessageType.IMAGE) {
                                val image = org.jetbrains.skia.Image.makeFromEncoded(file.data)
                                imageBitmap = image.toComposeImageBitmap()
                            }
                        } else {
                            val packet =
                                client.sendPacket(FileDownloadRequestPacket(client.token!!, data))
                            if (packet is FileDownloadResponsePacket)
                                if (packet.file.filename.isNotEmpty()) {
                                    if (packet.hash == packet.file.data.sha256()) {
                                        localFileQueries.insert(
                                            data,
                                            packet.file.filename,
                                            packet.file.data,
                                            packet.hash,
                                        )
                                        file = packet.file
                                        if (renderMessage.type == MessageType.IMAGE) {
                                            val image = org.jetbrains.skia.Image.makeFromEncoded(file.data)
                                            imageBitmap = image.toComposeImageBitmap()
                                        }
                                    }
                                }
                        }
                    } else {
                        val packet =
                            client.sendPacket(FileDownloadRequestPacket(client.token!!, data))
                        if (packet is FileDownloadResponsePacket)
                            if (packet.file.filename.isNotEmpty()) {
                                if (packet.hash == packet.file.data.sha256()) {
                                    file = packet.file
                                    if (renderMessage.type == MessageType.IMAGE) {
                                        val image = org.jetbrains.skia.Image.makeFromEncoded(file.data)
                                        imageBitmap = image.toComposeImageBitmap()
                                    }
                                }
                            }
                    }
                    fileLoading = false
                }
            }
        }
        onDispose {
            // do not cancel it, because exception will make the sendPacket method think the packet is not sent
        }
    }

    when (renderMessage.type) {
        MessageType.TEXT -> {
            SelectionContainer {
                Text(
                    text = renderMessage.content + "#${renderMessage.timestamp}", style = MaterialTheme.typography.body1
                )
            }
        }

        MessageType.FILE -> {
            if (fileLoading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp))
            else {
                if (file.filename.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp, 100.dp)
                            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "File is uploading",
                            style = MaterialTheme.typography.body2,
                            color = Color.Red
                        )
                    }
                } else
                    Column(
                        modifier = Modifier.size(100.dp, 100.dp)
                            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                            .clickable {
                                requestDownload = true
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Icon(
                            painter = painterResource("icons/description.svg"),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier.padding(top = 4.dp).horizontalScroll(rememberScrollState())
                        ) {
                            SelectionContainer {
                                Text(
                                    text = file.filename,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
            }
        }

        MessageType.IMAGE -> {
            if (fileLoading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(4.dp))
            else if (imageBitmap == null) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image is broken",
                        style = MaterialTheme.typography.body2,
                        color = Color.Red
                    )
                }
            } else {
                ContextMenuArea({
                    listOf(
                        ContextMenuItem("chat.download".l) {
                            requestDownload = true
                        }
                    )
                }) {
                    Image(painter = BitmapPainter(imageBitmap!!),
                        contentDescription = null,
                        modifier = Modifier
                            .height(imageBitmap!!.height.dp)
                            .width(imageBitmap!!.width.dp)
                            .clickable { showDialog = true }
                    )
                }
            }

        }
    }

    row {
        Icon(
            painter = when (renderMessage.internalId) {
                -1 -> painterResource("icons/cloud_upload.svg")
                -2 -> painterResource("icons/error.svg")
                else -> painterResource("icons/check_circle.svg")
            },
            contentDescription = "Status",
            modifier = Modifier.size(16.dp).padding(2.dp).align(Alignment.Bottom),
            tint = when (renderMessage.internalId) {
                -1 -> Color(0xFFE0E0E0)
                -2 -> Color(0xFFE57373)
                else -> Color(0xFF81C784)
            },
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = BitmapPainter(imageBitmap!!), contentDescription = null, modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun LangFile.LangScope.MessageView(renderMessage: RenderMessage) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
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
            Row(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        10.dp,
                        10.dp,
                        if (renderMessage.contactMessage) 10.dp else 0.dp,
                        if (renderMessage.contactMessage) 0.dp else 10.dp
                    )
                ).background(if (renderMessage.contactMessage) OTHER_MESSAGE_COLOR else MY_MESSAGE_COLOR).padding(10.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                useRow {
                    MessageContentView(renderMessage)
                }
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
fun LangFile.ColumnLangScope.ChatView(client: Client, contact: Contact) {

    var messageContent: RawMessageContent? by remember { mutableStateOf(null) }
    var sendRequest by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val dialog = remember { FocessDialog() }
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
                            copyMessageContent.toMessageContent(), false, (System.currentTimeMillis() / 1000).toInt()
                        )
                        messages.add(textRenderMessage)
                        delay(2000)
                        client.sendMessage(copyMessageContent)?.let {
                            list.add(it)
                            textRenderMessage.internalId = it.internalId
                            contact.messages.add(it)
                        }
                    } else {
                        val jobs = mutableListOf<Job>()
                        for (content in copyMessageContent.rawMessageContents) if (content !is RawRichMessageContent) {
                            launch {
                                val renderMessage = RenderMessage(
                                    content.toMessageContent(), false, (System.currentTimeMillis() / 1000).toInt()
                                )
                                messages.add(renderMessage)
                                client.sendMessage(content)?.let {
                                    list.add(it)
                                    renderMessage.internalId = it.internalId
                                    renderMessage.content = it.content.content
                                    contact.messages.add(it)
                                }
                            }.let {
                                jobs.add(it)
                            }
                        }
                        jobs.joinAll()
                    }
                    list
                }
                if (client.id != 0)
                    for (message in messageList) {
                        localMessageQueries.insert(
                            message.id.toLong(),
                            message.from.toLong(),
                            message.to.toLong(),
                            message.content.content,
                            message.content.type,
                            message.timestamp.toLong(),
                            message.internalId.toLong(),
                        )
                    }

                // indicate send failed
                messages.filter { it.internalId == -1 }.forEach {
                    it.internalId = -2
                }

                if (messageList.size != rawMessageCount) {
                    dialog.title = "chat.sendFailed".l
                    dialog.message = "chat.sendFailedMessage".l
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
                if (messages.find { it.internalId == message.internalId } != null) continue
                messages.add(
                    RenderMessage(
                        message.content, message.to == client.self!!.id, message.timestamp, message.internalId
                    )
                )
                lastSize = i + 1
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) requestShowLatestMessage = true
    }

    LaunchedEffect(requestShowLatestMessage) {
        if (requestShowLatestMessage) {
            val targetIndex = messages.size - 1
            if (targetIndex >= 0) listState.animateScrollToItem(targetIndex)
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
        modifier = Modifier.size(120.dp)
            .background(Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
    ) {

        Box(
            modifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            when (content) {
                is RawImageMessageContent -> {
                    val image = content.image
                    Image(painter = image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clickable { showDialog = true })
                }

                is RawFileMessageContent -> {
                    Column(
                        modifier = Modifier.fillMaxSize().background(
                            Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp)
                        ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource("icons/description.svg"),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier.padding(top = 4.dp).horizontalScroll(rememberScrollState())
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

        Icon(imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp).clickable { onRemove() })
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (content is RawImageMessageContent) {
                    Image(
                        painter = content.image, contentDescription = null, modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LangFile.ColumnLangScope.InputView(
    _sendRequest: Boolean,
    onDrag: () -> Unit,
    onMessageSend: (RawMessageContent) -> Unit
) {

    var sendRequest by remember { mutableStateOf(false) }
    var textMessage by remember { mutableStateOf("") }
    val currentFileMessageContents = remember { mutableStateListOf<RawFileMessageContent>() }
    val needRemoveFileMessageContents = remember { mutableStateListOf<RawFileMessageContent>() }
    var isDroppable by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(sendRequest) {
        if (sendRequest) {
            if (currentFileMessageContents.isEmpty()) onMessageSend(RawTextMessageContent(textMessage))
            else if (textMessage.isEmpty()) onMessageSend(RawRichMessageContent(*currentFileMessageContents.toTypedArray()))
            else onMessageSend(
                RawRichMessageContent(
                    RawTextMessageContent(textMessage),
                    *currentFileMessageContents.toTypedArray()
                )
            )
            textMessage = ""
            currentFileMessageContents.clear()
            sendRequest = false
        }
    }

    LaunchedEffect(currentFileMessageContents.size, isDroppable) {
        val targetIndex = if (isDroppable) currentFileMessageContents.size else currentFileMessageContents.size - 1
        if (targetIndex >= 0) listState.animateScrollToItem(index = targetIndex)
    }

    LaunchedEffect(needRemoveFileMessageContents.size) {
        if (needRemoveFileMessageContents.isNotEmpty()) {
            currentFileMessageContents.removeAll(needRemoveFileMessageContents)
            needRemoveFileMessageContents.clear()
        }
    }

    fun shouldSend(): Boolean {
        if (_sendRequest) return false
        if (textMessage.isNotEmpty()) return true
        if (currentFileMessageContents.isNotEmpty()) return true
        return false
    }

    fun canDrop(externalDragValue: ExternalDragValue) : Boolean {
        return externalDragValue.dragData is DragData.FilesList || externalDragValue.dragData is DragData.Image
    }

    Column(modifier = Modifier.wrapContentHeight(Alignment.Bottom).fillMaxWidth().onExternalDrag(onDragStart = {
        isDroppable = canDrop(it)
        if (isDroppable) onDrag()
    }, onDragExit = {
        isDroppable = false
    }, onDrop = {
        isDroppable = false
        val dragData = it.dragData
        if (dragData is DragData.FilesList) {
            val uris = dragData.readFiles()
            for (uri in uris) {
                val file = URI.create(uri).toPath().toFile()
                if (!file.exists() || file.isDirectory) continue
                currentFileMessageContents.add(
                    RawFileMessageContent(
                        File(
                            file.name,
                            Files.readAllBytes(file.toPath())
                        )
                    )
                )
            }
        } else if (dragData is DragData.Image) currentFileMessageContents.add(
            RawImageMessageContent(
                dragData.readImage()
            )
        )
    }

    )) {

        if (currentFileMessageContents.isNotEmpty() || isDroppable) {
            Box(
                modifier = Modifier.fillMaxWidth().height(125.dp).padding(8.dp)
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
                                modifier = Modifier.size(120.dp)
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }

        TextField(value = textMessage,
            onValueChange = { textMessage = it },
            modifier = Modifier.fillMaxWidth().onKeyEvent {
                if (it.key == Key.Enter) {
                    if (sendRequest) return@onKeyEvent true
                    if (shouldSend()) sendRequest = true
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
                        modifier = Modifier.clickable {
                            if (sendRequest) return@clickable
                            sendRequest = true
                        }.clip(RoundedCornerShape(5.dp)).padding(10.dp).pointerHoverIcon(PointerIcon.Hand),
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
            })
    }
}

internal fun LocalMessage.toMessage(): Message = Message(
    this.id.toInt(), this.sender.toInt(), this.receiver_.toInt(), this.internal_id.toInt(), when (this.type) {
        MessageType.TEXT -> TextMessageContent(this.data_)
        MessageType.IMAGE -> ImageMessageContent(this.data_)
        MessageType.FILE -> FileMessageContent(this.data_)
    }, this.timestamp.toInt()
)

internal fun convertTimestamp(timestamp: Int): String {
    val date = Date(timestamp.toLong() * 1000) // Convert to milliseconds
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}