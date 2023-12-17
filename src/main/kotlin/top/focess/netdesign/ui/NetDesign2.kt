package top.focess.netdesign.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import top.focess.netdesign.Database
import top.focess.netdesign.config.FileConfiguration
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.config.LangFile.Companion.createLandScope
import top.focess.netdesign.server.Contact
import top.focess.netdesign.server.GlobalState.server
import top.focess.netdesign.server.GlobalState.singleServer
import top.focess.netdesign.server.MessageType
import top.focess.netdesign.server.SingleServer
import top.focess.netdesign.sqldelight.message.LocalMessage
import top.focess.netdesign.sqldelight.message.ServerMessage
import top.focess.util.option.OptionParserClassifier
import top.focess.util.option.Options
import top.focess.util.option.type.IntegerOptionType
import top.focess.util.option.type.OptionType
import java.awt.EventQueue
import java.io.File

private fun loadConfiguration() : FileConfiguration {
    val file = File("config.yml")
    if (!file.exists())
        file.createNewFile()
    return FileConfiguration.loadFile(file);
}

val configuration = loadConfiguration()

val driver: SqlDriver = JdbcSqliteDriver(
    "jdbc:sqlite:netdesign.db"
).apply {
    val section = configuration.getSection("database")
    val currentVersion = section.getOrDefault("version", 0)
    val newVersion = Database.Schema.version

    if (newVersion.toInt() == 0)
        Database.Schema.create(this)
    else if (currentVersion < newVersion)
        Database.Schema.migrate(this, currentVersion.toLong(), newVersion)
    section["version"] = newVersion
}

object MessageTypeAdapter : ColumnAdapter<MessageType, String> {
    override fun decode(databaseValue: String) = MessageType.valueOf(databaseValue)
    override fun encode(value: MessageType) = value.name
}

val database = Database(
    driver,
    LocalMessage.Adapter(MessageTypeAdapter),
    ServerMessage.Adapter(MessageTypeAdapter)
)
val friendQueries = database.friendQueries
val localMessageQueries = database.localMessageQueries
val serverMessageQueries = database.serverMessageQueries
val fileQueries = database.fileQueries
val localFileQueries = database.localFileQueries

@Composable
fun rememberCenterWindowState(size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)): WindowState =
    rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

@Preview
fun main(args: Array<String>) {

    val options = Options.parse(args,
        OptionParserClassifier("local", OptionType.DEFAULT_OPTION_TYPE, IntegerOptionType.INTEGER_OPTION_TYPE),
    )

    val localOption = options["local"]
    if (localOption != null) {
        val name = localOption[OptionType.DEFAULT_OPTION_TYPE]
        val port = localOption[IntegerOptionType.INTEGER_OPTION_TYPE]
        singleServer = SingleServer(name, port, System.getenv("OPENAI_API_KEY"))

        // todo take over the RemoteServer
    }


    application(exitProcessOnExit = false) {

        var showSettings by remember { mutableStateOf(false) }
        var showRegister by remember { mutableStateOf(false) }
        var showTray by remember { mutableStateOf(false) }

        var currentContact by remember { mutableStateOf<Contact?>(null) }

        val alwaysOnTop by remember { mutableStateOf(false) }


        LaunchedEffect(server.host, server.port) {
            if (!server.connected)
                server.connect()
        }

        LaunchedEffect(server.self) {
            if (server.self == null)
                currentContact = null
        }

        createLandScope(LangFile("langs/zh_CN.yml")) {


            if (currentContact != null) {
                DefaultView(
                    onCloseRequest = { currentContact = null },
                    state = rememberCenterWindowState(DpSize(600.dp, 680.dp)),
                    title = "${currentContact?.name} #${currentContact?.id}",
                    alwaysOnTop = alwaysOnTop
                ) {

                    useWindow {
                        LaunchedEffect(currentContact) {
                            if (currentContact != null) {
                                EventQueue.invokeLater {
                                    window.toFront()
                                }
                            }
                        }
                    }


                    useColumn {
                        currentContact?.let {
                            useColumn {
                                ChatView(server, currentContact!!)
                            }
                        }
                    }
                }
            }

            if (server.self == null)
                DefaultView(
                    onCloseRequest = ::exitApplication,
                    state = rememberCenterWindowState(DpSize(600.dp, Dp.Unspecified)),
                    title = "login.title".l
                ) {
                    LoginView(server, {
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
                SurfaceView(
                    onCloseRequest = { showRegister = false },
                    state = rememberCenterWindowState(DpSize(600.dp, Dp.Unspecified)),
                    title = "register.title".l) {
                    RegisterView(server) {
                        showRegister = false
                    }
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

    println("Saving configuration...")
    configuration.save()

    singleServer?.close()
    server.close()
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}
