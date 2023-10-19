import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
fun LoginView(logined: () -> Unit = {}) {

    var username by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Username") },
            singleLine = true)
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text("Password") },
            singleLine = true)
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(onClick = { logined() }, modifier = Modifier.padding(16.dp)) {
            Text("Login")
        }

        Button(onClick = { }, modifier = Modifier.padding(16.dp)) {
            Text("Register")
        }

        Button(onClick = { }, modifier = Modifier.padding(16.dp)) {
            Text("Settings")
        }
    }
}

@Composable
fun MainView() {
    MaterialTheme {
        Text("Hello, World!")
    }
}


fun main() {

    val socket = Socket.getSocket()

    val screenSize = java.awt.Dimension(java.awt.Toolkit.getDefaultToolkit().screenSize)

    val loginViewWidth = 500.dp

    val loginViewHeight = 300.dp

    val loginViewWindowPosition =
        WindowPosition(((screenSize.width.dp - loginViewWidth) / 2), ((screenSize.height.dp - loginViewHeight) / 2))

    application(exitProcessOnExit = false) {
        var login by remember { mutableStateOf(false) }

        if (!login)
            Window(
                onCloseRequest = ::exitApplication, state = rememberWindowState(
                    position = loginViewWindowPosition,
                    size = DpSize(loginViewWidth, Dp.Unspecified)
                ), title = "NetDesign2 - Login", resizable = false, undecorated = true, transparent = true
            ) {

                MaterialTheme(colors = DefaultTheme.colors()) {

                        Surface(Modifier.clip(RoundedCornerShape(5.dp))) {

                            Column {
                                WindowDraggableArea {
                                    CustomLayout(
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                            .background(MaterialTheme.colors.primary),
                                        how = { measurables, constraints ->
                                            val title = measurables[0]
                                                .measure(Constraints( 0, constraints.maxWidth / 3, constraints.minHeight, constraints.maxHeight))

                                            title.place(constraints.maxWidth / 2 - title.width / 2,  20)

                                            val exit = measurables[1]
                                                .measure(Constraints( 0, constraints.maxWidth / 3, constraints.minHeight, constraints.maxHeight))
                                            exit.place(constraints.maxWidth - exit.width, 0)
                                        }
                                    ) {
                                        Text(text = "NetDesign2", fontSize = 24.sp)
                                        Button(modifier = Modifier.fillMaxHeight(), onClick = ::exitApplication) {
                                            Text("X")
                                        }
                                    }
                                }

                                LoginView {
                                    login = true
                                }
                            }
                        }
                }
            }
        else
            Window(title = "NetDesign2", onCloseRequest = ::exitApplication) {
                MainView()
            }
    }

}
