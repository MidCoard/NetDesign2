package top.focess.netdesign.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.*
import top.focess.netdesign.server.GlobalState.contacts


@Composable
fun LangFile.LangScope.MainView(client: Client, showContact: (Contact) -> Unit = {}) {

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {

        item {
            client.self?.let {
                MyView(client.self!!)
            }
        }

        items(contacts, key = {it.id}) { contact ->
            if (contact is Friend && contact.id != client.id) {
                FriendView(contact, showContact)
            } else if (contact is Group) {
                GroupView(contact)
            }
        }
    }
}

@Composable
fun MyView(self: Friend) {

    Surface {
        SelectionContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.Person,
                    contentDescription = self.name,
                    modifier = Modifier
                        .size(125.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text(text = self.name, style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold))
                    Text(text = "#${self.id}", style = TextStyle(fontSize = 15.sp))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LangFile.LangScope.FriendView(friend: Friend, showContact: (Contact) -> Unit) {

    var isHovered by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(if (isHovered) Color.LightGray else DefaultTheme.colors().background)
    var lastMessage : Message? by remember { mutableStateOf(null) }

    LaunchedEffect(friend.messages.size) {
        lastMessage = friend.messages.maxByOrNull { it.internalId }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { showContact(friend) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            Icons.Default.Person,
            contentDescription = friend.name,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = friend.name, style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold))
            Text(text = lastMessageView(lastMessage), style = TextStyle(fontSize = 14.sp))
        }

    }
}


@Composable
fun GroupView(group: Group) {
    Text(group.name)
}

private fun LangFile.LangScope.lastMessageView(message: Message?) : String {
    return message?.let {
        when (it.content.type) {
            MessageType.TEXT -> {
                it.content.content.maxLength(20)
            }

            MessageType.IMAGE -> {
                "chat.imageMessage".l
            }

            MessageType.FILE -> {
                "chat.fileMessage".l
            }
        }
    } ?: ""
}

internal fun queryLatestLocalMessages(a: Long, b: Long): List<Message> {
    val messages = localMessageQueries.selectLatest(a, b).executeAsList().map { it.toMessage() }.toMutableList()
    messages
        .addAll(localMessageQueries.selectLatest(b, a).executeAsList().map { it.toMessage() })
    return messages
}

private fun queryNewestLocalMessage(a: Long, b: Long): Message? {
    val message = localMessageQueries.selectNewest(a, b).executeAsOneOrNull()?.toMessage()
    val message2 = localMessageQueries.selectNewest(b, a).executeAsOneOrNull()?.toMessage()

    if (message != null && message2 != null)
        return if (message.internalId > message2.internalId) message else message2

    return message ?: message2
}

internal fun String.maxLength(maxLength: Int) = if (this.length > maxLength) this.substring(0, maxLength) + "..." else this