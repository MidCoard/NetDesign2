package top.focess.netdesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.RemoteServer
import top.focess.netdesign.server.packet.RegisterRequestPacket
import top.focess.netdesign.server.packet.RegisterResponsePacket


fun canRegister(username: String, password: String, confirmPassword: String) =
    username.length in 6..20 && password.length in 6..20 && password == confirmPassword

@Composable
fun LangFile.LangScope.RegisterView(server: RemoteServer, registered: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var registerRequest by remember { mutableStateOf(false) }

    val showDialog = remember { mutableStateOf(false) }

    var dialog by remember { mutableStateOf(FocessDialog(show = showDialog)) }

    LaunchedEffect(registerRequest) {
        if (registerRequest) {
            var flag = false
            if (canRegister(username, password, confirmPassword)) {
                val packet = server.sendPacket(RegisterRequestPacket(username, password))
                println(packet)
                if (packet is RegisterResponsePacket && packet.success) {
                    flag = true
                }
            }

            dialog = if (!flag)
                FocessDialog("register.registerFailed".l, "register.registerFailedMessage".l, showDialog)
            else
                FocessDialog("register.registerSuccess".l, "register.registerSuccessMessage".l, showDialog)
            dialog.show()
            registerRequest = false
        }
    }

    LaunchedEffect(showDialog.value) {
        if (!showDialog.value && dialog.title == "register.registerSuccess".l)
            registered()
    }

    FocessDialogWindow(dialog)

    Spacer(Modifier.padding(10.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("register.username".l) },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        PasswordTextField(
            password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("register.password".l) },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        PasswordTextField(
            confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("register.confirmPassword".l) },
            singleLine = true,
            onEnterKey = {
                registerRequest = true
            }
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = {
                registerRequest = true
            },
            modifier = Modifier.padding(16.dp),
            enabled = !registerRequest && canRegister(username, password, confirmPassword) && !showDialog.value
        ) {
            Text("register.register".l)
        }
    }


}