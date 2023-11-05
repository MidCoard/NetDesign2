import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.proto.PacketOuterClass
import top.focess.netdesign.proto.PacketOuterClass.ContactListRequest
import top.focess.netdesign.proto.loginRequest
import top.focess.netdesign.server.*
import top.focess.netdesign.server.packet.*
import top.focess.netdesign.ui.DefaultView
import top.focess.netdesign.ui.IntTextField
import top.focess.netdesign.ui.ProgressionIcon
import top.focess.netdesign.ui.SurfaceView
import java.awt.TrayIcon
import java.security.MessageDigest

@Composable
@Preview
fun LangFile.LangScope.LoginView(
    server: RemoteServer,
    logined: () -> Unit = {},
    showSettings: () -> Unit = {},
    showRegister: () -> Unit = {}
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var reconnect by remember { mutableStateOf(false) }

    var loginRequest by remember { mutableStateOf(false) }

    LaunchedEffect(reconnect) {
        if (reconnect) {
            delay(1000)
            server.reconnect()
            reconnect = false
        }
    }


    LaunchedEffect(loginRequest) {
        if (loginRequest) {
            if (server.connected()) {
                val packet = server.sendPacket(LoginPreRequestPacket(username))
                if (packet is LoginPreResponsePacket) {
                    val rawPassword = password + packet.challenge
                    val encryptedPassword = rawPassword.sha256()
                    val loginPacket = server.sendPacket(LoginRequestPacket(username, encryptedPassword))
                    if (loginPacket is LoginResponsePacket && loginPacket.logined)
                        logined()
                }
            }
            loginRequest = false
        }
    }

    Spacer(Modifier.padding(10.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("login.username".l) },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("login.password".l) },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { loginRequest = true },
            modifier = Modifier.padding(16.dp),
            enabled = server.connected() && !loginRequest
        ) {
            Text("login.login".l)
            Spacer(Modifier.width(5.dp))

            Crossfade(server.connected) {
                if (server.connected())
                    Icon(Icons.Default.Done, "login.connected".l)
                else if (!server.connected)
                    Icon(Icons.Default.Close, "login.disconnected".l)
                else ProgressionIcon()
            }
        }

        Button(
            onClick = { reconnect = true },
            modifier = Modifier.padding(16.dp),
            enabled = !reconnect && !server.connected
        ) {
            Icon(Icons.Default.Refresh, "login.reconnect".l)
        }

        Button(
            onClick = { showRegister() },
            modifier = Modifier.padding(16.dp),
            enabled = server.connected() && server.registerable
        ) {
            Text("login.register".l)
            Spacer(Modifier.width(5.dp))
            Crossfade(server.connected() && server.registerable) {
                if (it)
                    Icon(Icons.Default.Done, "login.connected".l)
                else
                    Icon(Icons.Default.Close, "login.disconnected".l)
            }
        }

        Button(onClick = { showSettings() }, modifier = Modifier.padding(16.dp)) {
            Text("login.settings".l)
        }
    }
}


@Composable
fun LangFile.LangScope.SettingsView(_host: String, _port: Int, saveSettings: (host: String, port: Int) -> Unit) {
    var host by remember { mutableStateOf(_host) }
    var port by remember { mutableStateOf(_port) }
    var save by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            host,
            onValueChange = { host = it },
            modifier = Modifier.fillMaxWidth(0.8f).weight(2f),
            label = { Text("settings.host".l) },
            singleLine = true
        )

        Spacer(Modifier.width(8.dp))

        IntTextField(
            port.toString(),
            port,
            onValueChange = { port = it },
            modifier = Modifier.fillMaxWidth(0.8f).weight(1f),
            label = { Text("settings.port".l) },
            singleLine = true,
            left = 0,
            right = 65535
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(onClick = {
            save = true
        }) {
            Text("settings.save".l)
        }
    }

    LaunchedEffect(save) {
        if (save) {
            delay(200)
            saveSettings(host, port)
        }
    }

}

@Composable
fun RegisterView() {

}


@Composable
fun MainView(server : RemoteServer) {

    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val packet = server.sendPacket(ContactListRequestPacket())
        if (packet is ContactListResponsePacket) {
            val friendList = mutableListOf<Friend>()
            val groupList = mutableListOf<Group>()
            val groupMap = mutableMapOf<Int, Group>()
            for (contact in packet.contacts) {
                if (contact.type == PacketOuterClass.Contact.ContactType.FRIEND) {
                    val contactPacket = server.sendPacket(FriendInfoRequestPacket(contact.id))
                    if (contactPacket is FriendInfoResponsePacket)
                        friendList.add(Friend(contact.id, contact.name))
                } else if (contact.type == PacketOuterClass.Contact.ContactType.GROUP) {
                    val contactPacket = server.sendPacket(GroupInfoRequestPacket(contact.id))
                    if (contactPacket is GroupInfoResponsePacket) {
                        val group = Group(contact.id, contact.name, contactPacket.members)
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

        }
    }

    TextField(
        text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        label = { Text("Text") },
        singleLine = true
    )
}

@Composable
fun rememberCenterWindowState(size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)): WindowState =
    rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

private fun <T> compareAndAddOrRemove(list: MutableList<T>, newList: List<T>, except: (T) -> Boolean = { false }) {
    val toAdd = newList.filter { !list.contains(it) }
    val toRemove = list.filter  { !newList.contains(it) || except(it) }
    list.addAll(toAdd)
    list.removeAll(toRemove)
}

private fun String.sha256(): String = hashString(this, "SHA-256")

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}

fun main() {

    SingleServer("Local")

    val l = LangFile("langs/zh_CN.yml");

    LangFile.createLandScope(l) {

        application(exitProcessOnExit = true) {
            val server = rememberSaveable(saver = RemoteServer.Saver()) { RemoteServer() }

            var logined by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            var showRegister by remember { mutableStateOf(false) }
            var showTray by remember { mutableStateOf(false) }

            LaunchedEffect(server.host, server.port) {
                if (!server.connected)
                    server.connect()
            }

            if (!logined)
                DefaultView(
                    onCloseRequest = ::exitApplication,
                    state = rememberCenterWindowState(DpSize(500.dp, Dp.Unspecified)),
                    title = "login.title".l
                ) {
                    LoginView(server, {
                        logined = true
                    }, {
                        showSettings = true
                    }, {
                        showRegister = true
                    })
                }
            else if (!showTray)
                DefaultView(
                    onCloseRequest = { showTray = true },
                    state = rememberCenterWindowState(DpSize(400.dp, 720.dp)),
                    title = "title".l
                ) {
                    MainView(server)
                }
            else
                Tray(
                    state = rememberTrayState(),
                    icon = TrayIcon,
                    menu = {
                        Item("tray.show".l) {
                            showTray = false
                        }
                        Item("tray.quit".l) {
                            exitApplication()
                        }
                    }
                )


            if (showRegister) {
                if (server.registerable)
                    SurfaceView(onCloseRequest = { showRegister = false }, title = "register.title".l) {
                        RegisterView()
                    }
                else {
                    showRegister = false
                    println("not registerable")
                }
            }

            if (showSettings) {
                SurfaceView(
                    onCloseRequest = { showSettings = false },
                    state = rememberCenterWindowState(DpSize(400.dp, Dp.Unspecified)),
                    title = "settings.title".l,
                ) {
                    SettingsView(server.host, server.port) { host, port ->
                        showSettings = false
                        if (server.host != host || server.port != port)
                            server.disconnect()
                        server.host = host
                        server.port = port
                    }
                }
            }
        }
    }

}
