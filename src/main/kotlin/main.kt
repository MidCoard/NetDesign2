import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import top.focess.netdesign.Socket
import top.focess.netdesign.ui.DefaultTheme


@Composable
fun CustomLayout(
    modifier: Modifier,
    how: Placeable.PlacementScope.(measurables: List<Measurable>, constraints: Constraints) -> Unit,
    children: @Composable () -> Unit
) = Layout({ children() }, modifier) { measurables, constraints ->
    layout(constraints.maxWidth, constraints.maxHeight) {
        how(measurables, constraints)
    }
}

@Composable
@Preview
fun LoginView(logined: () -> Unit = {}, showSettings: () -> Unit = {}, showRegister: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

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
fun DefaultView(
    state: WindowState = rememberWindowState(),
    title: String,
    onCloseRequest: () -> Unit = {},
    children: @Composable () -> Unit
) {

    Window(
        onCloseRequest = {}, state = state, title = title, transparent = true,
        undecorated = true
    ) {

        MaterialTheme(colors = DefaultTheme.colors()) {

            Surface(Modifier.clip(RoundedCornerShape(5.dp))) {

                Column {

                    WindowDraggableArea {

                        CustomLayout(
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                .background(MaterialTheme.colors.primary),
                            how = { measurables, constraints ->
                                val titleBar = measurables[0]
                                    .measure(
                                        Constraints(
                                            0,
                                            constraints.maxWidth / 3,
                                            constraints.minHeight,
                                            constraints.maxHeight
                                        )
                                    )

                                titleBar.place(constraints.maxWidth / 2 - titleBar.width / 2, 25)

                                val exit = measurables[1]
                                    .measure(
                                        Constraints(
                                            0,
                                            constraints.maxWidth / 3,
                                            constraints.minHeight,
                                            constraints.maxHeight
                                        )
                                    )
                                exit.place(constraints.maxWidth - exit.width, 0)
                            }
                        ) {
                            Text(text = title, fontSize = 16.sp)
                            Button(modifier = Modifier.fillMaxHeight(), onClick = onCloseRequest) {
                                Text("X")
                            }
                        }
                    }
                    children()
                }
            }
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
fun centerWindowState(width: Dp, _height: Dp): WindowState {
    var height = _height;
    val unspecified = _height == Dp.Unspecified;
    if (unspecified)
        height = 0.dp
    val screenSize = java.awt.Dimension(java.awt.Toolkit.getDefaultToolkit().screenSize)
    val windowPosition = WindowPosition(((screenSize.width.dp - width) / 2), ((screenSize.height.dp - height) / 2))
    return rememberWindowState(position = windowPosition, size = DpSize(width, if (unspecified) Dp.Unspecified else height))
}

fun main() {

    val socket = Socket.getSocket()

    application(exitProcessOnExit = false) {
        var login by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showRegister by remember { mutableStateOf(false) }

        if (!login)
            DefaultView(onCloseRequest = ::exitApplication, state = centerWindowState(500.dp, Dp.Unspecified), title = "NetDesign2 - Login") {
                LoginView({
                    login = true
                }, {
                    showSettings = true
                }, {
                    showRegister = true
                })
            }
        else
            DefaultView(title = "NetDesign2") {
                MainView()
            }

        if (showSettings) {
            DefaultView(onCloseRequest = { showSettings = false }, title = "NetDesign2 - Settings") {
                SettingsView();
            }
        }

        if (showRegister) {
            DefaultView(onCloseRequest = { showRegister = false }, title = "NetDesign2 - Register") {
                RegisterView();
            }
        }
    }

}
