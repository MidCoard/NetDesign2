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
import top.focess.netdesign.server.*
import top.focess.netdesign.server.packet.ContactListRequestPacket
import top.focess.netdesign.server.packet.ContactListResponsePacket


@Composable
fun MainView(server: RemoteServer, showContact: (Contact) -> Unit = {}) {

    LaunchedEffect(Unit) {
        while (true) {
            val packet = server.sendPacket(ContactListRequestPacket())
            if (packet is ContactListResponsePacket) {
                server.self =
                    packet.contacts.filterIsInstance<Friend>().find { it.name == server.lastLoginedUsername }!!
                val filtered = packet.contacts.filter { it.name != server.lastLoginedUsername }
                val contactMap = filtered.associateBy { it.id }
                val visitedMap = mutableMapOf<Int, Boolean>()
                for (contact in filtered)
                    visitedMap[contact.id] = false

                val toRemove = mutableListOf<Contact>()

                for (contact in contacts) {
                    if (contact is Friend) {
                        val target = contactMap[contact.id]
                        if (target is Friend) {
                            visitedMap[contact.id] = true
                            contact.name = target.name
                            contact.online = target.online
                        } else toRemove.add(contact)
                    } else if (contact is Group) {
                        val target = contactMap[contact.id]
                        if (target is Group) {
                            visitedMap[contact.id] = true
                            contact.name = target.name
                            contact.online = target.online
                            val visitedMemberMap = mutableMapOf<Int, Boolean>()
                            for (member in target.members)
                                visitedMemberMap[member.id] = false
                            val toRemoveMember = mutableListOf<Member>()
                            for (member in contact.members) {
                                val targetMember = target.members.find { it.id == member.id }
                                if (targetMember != null) {
                                    visitedMemberMap[member.id] = true
                                    member.name = targetMember.name
                                    member.online = targetMember.online
                                } else toRemoveMember.add(member)
                            }
                            contact.members.removeAll(toRemoveMember)
                            contact.members.addAll(target.members.filter { !visitedMemberMap[it.id]!! })
                        } else toRemove.add(contact)
                    }
                }

                contacts.removeAll(toRemove)
                contacts.addAll(filtered.filter { !visitedMap[it.id]!! })

                contacts.add(Friend(1, "WWWWWWWWWWWWWWWWWWWW", true))
                contacts.add(Friend(2, "test2", false))

            }
            delay(10000)
        }
    }

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 4.dp),
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
            if (contact is Friend) {
                FriendView(contact, showContact)
            } else if (contact is Group) {
                GroupView(contact)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyView(self: Friend) {

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