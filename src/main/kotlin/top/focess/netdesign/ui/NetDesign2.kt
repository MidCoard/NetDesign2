package top.focess.netdesign.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.config.LangFile.Companion.createLandScope
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.RemoteServer
import top.focess.netdesign.server.SingleServer

@Composable
fun rememberCenterWindowState(size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)): WindowState =
    rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

@Preview
fun main() {

    SingleServer("Local")

    application(exitProcessOnExit = true) {

        val server = rememberSaveable(saver = RemoteServer.Saver()) { RemoteServer() }

        var logined by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showRegister by remember { mutableStateOf(false) }
        var showTray by remember { mutableStateOf(false) }

        var currentContact by remember { mutableStateOf<Contact?>(null) }

        var clicked by remember { mutableStateOf(0) }

        LaunchedEffect(server.host, server.port) {
            if (!server.connected)
                server.connect()
        }

        createLandScope(LangFile("langs/zh_CN.yml")) {

            if (currentContact != null) {
                DefaultView(
                    onCloseRequest = { currentContact = null },
                    state = rememberCenterWindowState(DpSize(600.dp, 680.dp)),
                    title = currentContact?.name ?: "title".l
                ) {
                    currentContact?.let {
                        enterColumn {
                            ChatView(currentContact!!)
                        }
                    }
                }
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
                    state = rememberCenterWindowState(DpSize(325.dp, 720.dp)),
                    title = "title".l
                ) {
                    MainView(server) {
                        currentContact = it
                    }
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

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}
