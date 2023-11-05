package top.focess.netdesign.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
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
import kotlinx.coroutines.delay
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.server.*
import top.focess.netdesign.server.packet.*


@Composable
fun MainView(server: RemoteServer, showContact: (Contact) -> Unit = {}) {

    LaunchedEffect(Unit) {
        while (true) {
            val packet = server.sendPacket(ContactListRequestPacket())
            if (packet is ContactListResponsePacket) {
                val friendList = mutableListOf<Contact>()
                val groupList = mutableListOf<Contact>()
                val groupMap = mutableMapOf<Int, Group>()
                for (contact in packet.contacts) {
                    if (contact.type == PacketOuterClass.Contact.ContactType.FRIEND) {
                        val contactPacket = server.sendPacket(FriendInfoRequestPacket(contact.id))
                        if (contactPacket is FriendInfoResponsePacket)
                            friendList.add(Friend(contact.id, contact.name, contact.online))
                    } else if (contact.type == PacketOuterClass.Contact.ContactType.GROUP) {
                        val contactPacket = server.sendPacket(GroupInfoRequestPacket(contact.id))
                        if (contactPacket is GroupInfoResponsePacket) {
                            val group = Group(contact.id, contact.name, contact.online, contactPacket.members)
                            groupList.add(group)
                            groupMap[contact.id] = group
                        }
                    }
                }

                compareAndAddOrRemove(contacts, friendList) {
                    it is Group
                }

                for (group in contacts.filterIsInstance<Group>()) {
                    val target = groupMap[group.id]
                    if (target != null)
                        compareAndAddOrRemove(group.members, target.members)
                }

                compareAndAddOrRemove(contacts, groupList) {
                    it is Friend
                }

                contacts.add(Friend(1, "test", true))
                contacts.add(Friend(2, "test2", false))

            }
            delay(1000)
        }
    }

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {

        val contactList = contacts.toList()

        item { MyView(server.lastLoginedUsername!!) }

        items(contactList) { contact ->
            if (contact is Friend) {
                FriendView(contact, showContact)
            } else if (contact is Group) {
                GroupView(contact)
            }
        }
    }
}

@Composable
fun MyView(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            Icons.Default.Person,
            contentDescription = username,
            modifier = Modifier
                .size(125.dp)
                .padding(8.dp)
                .clip(CircleShape)
        )
        Text(text = username, style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold))
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
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }.onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { showContact(friend)  },
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

private fun <T> compareAndAddOrRemove(list: MutableList<T>, newList: List<T>, except: (T) -> Boolean = { false }) {
    val toAdd = newList.filter { !list.contains(it) }
    val toRemove = list.filter { !newList.contains(it) && !except(it) }
    list.addAll(toAdd)
    list.removeAll(toRemove)
}