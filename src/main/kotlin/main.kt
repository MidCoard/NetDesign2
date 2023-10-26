import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import top.focess.netdesign.ServerConnection
import top.focess.netdesign.ui.DefaultView
import top.focess.netdesign.ui.SurfaceView

@Composable
@Preview
fun LoginView(logined: () -> Unit = {}, showSettings: () -> Unit = {}, showRegister: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    Spacer(Modifier.fillMaxWidth().padding(10.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Username") },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Password") },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(onClick = { logined() }, modifier = Modifier.padding(16.dp)) {
            Text("Login")
        }

        Button(onClick = { showRegister() }, modifier = Modifier.padding(16.dp)) {
            Text("Register")
        }

        Button(onClick = { showSettings() }, modifier = Modifier.padding(16.dp)) {
            Text("Settings")
        }
    }
}


@Composable
fun SettingsView() {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            host,
            onValueChange = { host = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Host") },
            singleLine = true
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            port,
            onValueChange = { port = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Port") },
            singleLine = true
        )
    }
}

@Composable
fun RegisterView() {

}


@Composable
fun MainView() {
    var text by remember { mutableStateOf("") }
    TextField(
        text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        label = { Text("Text") },
        singleLine = true
    )
}


@Composable
fun centerWindowState(width: Dp, height: Dp): WindowState {
    val screenSize = java.awt.Dimension(java.awt.Toolkit.getDefaultToolkit().screenSize)
    val windowPosition = WindowPosition(((screenSize.width.dp - width) / 2), ((screenSize.height.dp - height) / 2))
    return rememberWindowState(position = windowPosition, size = DpSize(width, height))
}

fun main() {

    val serverConnection = ServerConnection.getServerConnection()



    application(exitProcessOnExit = true) {
        var login by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showRegister by remember { mutableStateOf(false) }

        if (!login)
            DefaultView(onCloseRequest = ::exitApplication, state = centerWindowState(500.dp, 500.dp), title = "NetDesign2 - Login") {
                LoginView({
                    login = true
                }, {
                    showSettings = true
                }, {
                    showRegister = true
                })
            }
        else
            DefaultView(onCloseRequest = ::exitApplication, title = "NetDesign2") {
                MainView()
            }

        if (showSettings) {
            SurfaceView(onCloseRequest = { showSettings = false }, state = centerWindowState(500.dp, 400.dp), title = "NetDesign2 - Settings") {
                SettingsView()
            }
        }

        if (showRegister) {
            if (serverConnection.getServer() == null)

            else
                DefaultView(onCloseRequest = { showRegister = false }, title = "NetDesign2 - Register") {
                    RegisterView()
                }
        }
    }

}
