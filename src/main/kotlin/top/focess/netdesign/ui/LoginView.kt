package top.focess.netdesign.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.RemoteServer
import top.focess.netdesign.server.packet.LoginPreRequestPacket
import top.focess.netdesign.server.packet.LoginPreResponsePacket
import top.focess.netdesign.server.packet.LoginRequestPacket
import top.focess.netdesign.server.packet.LoginResponsePacket
import java.security.MessageDigest

fun canLogin(username: String, password: String) =
    username.length in 6..20 && password.length in 6..20

@Composable
fun LangFile.LangScope.LoginView(
    server: RemoteServer,
    showSettings: () -> Unit = {},
    showRegister: () -> Unit = {}
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var reconnect by remember { mutableStateOf(false) }
    var loginRequest by remember { mutableStateOf(false) }

    val dialog = remember { FocessDialog() }

    LaunchedEffect(reconnect) {
        if (reconnect) {
            delay(1000)
            server.reconnect()
            reconnect = false
        }
    }

    LaunchedEffect(loginRequest) {
        if (loginRequest) {
            var flag = false
            if (canLogin(username, password)) {
                val packet = server.sendPacket(LoginPreRequestPacket(username))
                if (packet is LoginPreResponsePacket) {
                    val rawPassword = password.sha256() + packet.challenge
                    val encryptedPassword = rawPassword.sha256()
                    val loginPacket = server.sendPacket(LoginRequestPacket(username, encryptedPassword))
                    if (loginPacket is LoginResponsePacket && loginPacket.logined) {
                        flag = true
                        server.setupChannel(loginPacket.id, username, loginPacket.token)
                    }
                }
            }
            if (!flag) {
                dialog.title  = "login.loginFailed".l
                dialog.message = "login.loginFailedMessage".l
                dialog.show()
            }
            loginRequest = false
        }
    }

    FocessDialogWindow(dialog)

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
        PasswordTextField(
            password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("login.password".l) },
            singleLine = true,
            onEnterKey = {
                loginRequest = true
            }
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = {
                loginRequest = true
                      },
            modifier = Modifier.padding(16.dp),
            enabled = server.connected() && !loginRequest && canLogin(username, password) && !dialog.show && server.channelSocket == null
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
            enabled = !reconnect && server.connected != RemoteServer.ConnectionStatus.CONNECTING
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

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

internal fun String.sha256(): String = hashString(this, "SHA-256")
