package top.focess.netdesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.RemoteClient
import top.focess.netdesign.server.packet.RegisterRequestPacket
import top.focess.netdesign.server.packet.RegisterResponsePacket


fun canRegister(username: String, password: String, confirmPassword: String) =
    username.length in 6..20 && password.length in 6..20 && password == confirmPassword

@Composable
fun LangFile.LangScope.RegisterView(server: RemoteClient, registered: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var registerRequest by remember { mutableStateOf(false) }

    val dialog = remember { FocessDialog() }

    LaunchedEffect(registerRequest) {
        if (registerRequest) {
            var flag = false
            if (canRegister(username, password, confirmPassword)) {
                val packet = server.sendPacket(RegisterRequestPacket(username, password))
                if (packet is RegisterResponsePacket && packet.success) {
                    flag = true
                }
            }

            dialog.title = if (!flag)
                "register.registerFailed".l
            else
                "register.registerSuccess".l
            dialog.message = if (!flag)
                "register.registerFailedMessage".l
            else
                "register.registerSuccessMessage".l
            dialog.show()
            registerRequest = false
        }
    }

    LaunchedEffect(dialog.show) {
        if (!dialog.show && dialog.title == "register.registerSuccess".l)
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
                if (!registerRequest && canRegister(username, password, confirmPassword) && !dialog.show)
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
            enabled = !registerRequest && canRegister(username, password, confirmPassword) && !dialog.show
        ) {
            Text("register.register".l)
        }
    }


}