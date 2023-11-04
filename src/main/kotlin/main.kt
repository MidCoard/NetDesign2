import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.server.RemoteServer
import top.focess.netdesign.server.SingleServer
import top.focess.netdesign.ui.DefaultView
import top.focess.netdesign.ui.IntTextField
import top.focess.netdesign.ui.SurfaceView
import java.util.*

@Composable
@Preview
fun LangFile.LandScope.LoginView(status : RemoteServer.ConnectionStatus, logined: () -> Unit = {}, showSettings: () -> Unit = {}, showRegister: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    Spacer(Modifier.fillMaxWidth().padding(10.dp))

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
        Button(onClick = { logined() }, modifier = Modifier.padding(16.dp)) {
            Text("login.login".l)
        }

        Button(onClick = { showRegister() }, modifier = Modifier.padding(16.dp)) {
            Text("login.register".l)
        }

        Button(onClick = { showSettings() }, modifier = Modifier.padding(16.dp)) {
            Text("login.settings".l)
        }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Text(status.toString())
    }
}


@Composable
fun LangFile.LandScope.SettingsView(_host :String, _port :Int, saveSettings: (host :String, port :Int) -> Unit) {
    var host by remember { mutableStateOf(_host) }
    var port by remember { mutableStateOf(_port) }
    var clicked by remember { mutableStateOf(false) }

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

    Row(modifier =  Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(onClick = {
            clicked = true
        }) {
            Text("settings.save".l)
        }
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(200)
            saveSettings(host, port)
        }
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
fun rememberCenterWindowState(size: DpSize): WindowState = rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

fun main() {

    SingleServer()

    val l = LangFile("langs/zh_CN.yml");

    LangFile.createLandScope(l) {

        application(exitProcessOnExit = true) {
            val server = rememberSaveable(saver = RemoteServer.Saver()) { RemoteServer() }
            var login by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            var showRegister by remember { mutableStateOf(false) }

            LaunchedEffect(server.host, server.port) {
                if (!server.connected)
                    server.connect()
            }

            if (!login)
                DefaultView(
                    onCloseRequest = ::exitApplication,
                    state = rememberCenterWindowState(size = DpSize(500.dp, Dp.Unspecified)),
                    title = "login.title".l
                ) {
                    LoginView(server.connected,{
                        login = true
                        showSettings = false
                        showRegister = false
                    }, {
                        showSettings = true
                    }, {
                        showRegister = true
                    })
                }
            else
                DefaultView(onCloseRequest = ::exitApplication, title = "title".l) {
                    MainView()
                }


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
