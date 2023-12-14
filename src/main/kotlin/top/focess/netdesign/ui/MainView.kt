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
import top.focess.netdesign.server.*


@Composable
fun MainView(server: RemoteServer, showContact: (Contact) -> Unit = {}) {

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {

        val contactList = contacts.toList()

        item {
            server.self?.let {
                MyView(server.self!!)
            }
        }

        items(contactList) { contact ->
            if (contact is Friend && contact.id != server.id) {
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
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text(text = self.name, style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold))
                    Text(text = "#${self.id}", style = TextStyle(fontSize = 15.sp))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FriendView(friend: Friend, showContact: (Contact) -> Unit) {

    var isHovered by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(if (isHovered) Color.LightGray else DefaultTheme.colors().background)

    

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
            Text(text = "last message", style = TextStyle(fontSize = 14.sp))
        }

    }
}


@Composable
fun GroupView(group: Group) {
    Text(group.name)
}