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
import com.google.common.io.Files
import top.focess.netdesign.Database
import top.focess.netdesign.config.FileConfiguration
import top.focess.netdesign.config.LangFile
import top.focess.netdesign.config.LangFile.Companion.createLandScope
import top.focess.netdesign.config.NetworkConfig.DEFAULT_SERVER_PORT
import top.focess.netdesign.config.Platform
import top.focess.netdesign.config.Platform.Companion.CURRENT_OS
import top.focess.netdesign.server.*
import top.focess.netdesign.server.GlobalState.client
import top.focess.netdesign.server.GlobalState.localServer
import top.focess.netdesign.sqldelight.message.LocalMessage
import top.focess.netdesign.sqldelight.message.ServerMessage
import top.focess.util.option.OptionParserClassifier
import top.focess.util.option.Options
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J
import java.awt.EventQueue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.rmi.Remote
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream


val osConfigDir: String = when (CURRENT_OS) {
    Platform.WINDOWS -> System.getenv("APPDATA")
    Platform.MACOS -> System.getProperty("user.home") + "/Library/Application Support"
    else -> System.getProperty("user.home") + "/.config" // Assume Linux
}

val configDir: String = "$osConfigDir/NetDesign2"

val configFile = File("$configDir/config.yml").let {
    if (!it.exists()) {
        it.parentFile.mkdirs()
        it.createNewFile()
    }
    it
}

val configuration = FileConfiguration.loadFile(configFile)

val driver: SqlDriver = JdbcSqliteDriver(
    "jdbc:sqlite:$configDir/netdesign.db"
).apply {
    val section = configuration.getSection("database")
    val currentVersion = section.getOrDefault("version", 0)
    val newVersion = Database.Schema.version

    try {
        if (currentVersion == 0) {
            Database.Schema.create(this)
            println("Created database.")
        } else if (currentVersion < newVersion)
            Database.Schema.migrate(this, currentVersion.toLong(), newVersion)
    } catch (e: Exception) {
        // ignore the create database error (database already exists)
        e.printStackTrace()
    }
    section["version"] = newVersion
    // make sure the database version is up-to-date.
    configuration.save()
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

val contactQueries = database.contactQueries
val friendQueries = database.friendQueries
val groupQueries = database.groupQueries
val localMessageQueries = database.localMessageQueries
val serverMessageQueries = database.serverMessageQueries
val fileQueries = database.fileQueries
val localFileQueries = database.localFileQueries

@Composable
fun rememberCenterWindowState(size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified)): WindowState =
    rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

@Preview
fun main(args: Array<String>) {
    SysOutOverSLF4J.sendSystemOutAndErrToSLF4J()

    val options = Options.parse(args, OptionParserClassifier("local"))
    val option = options.get("local")
    if (option != null) {
        localServer = LocalServer(name = "Local")
        client = localServer!!.getClient()
    } else {
        val section = configuration.getSection("local")
        if (section.getOrDefault("status", false)) {
            val name: String? = section["name"]
            val port: Int? = section.getOrDefault("port", DEFAULT_SERVER_PORT)
            name?.let {
                port?.let {
                    localServer =
                        LocalServer(name, port, section.getOrDefault("apiKey", System.getenv("OPENAI_API_KEY")))
                    client = localServer!!.getClient()
                }
            }
        }
    }

    application(exitProcessOnExit = false) {

        var showSettings by remember { mutableStateOf(false) }
        var showRegister by remember { mutableStateOf(false) }
        var showTray by remember { mutableStateOf(false) }

        var currentContact by remember { mutableStateOf<Contact?>(null) }

        val alwaysOnTop by remember { mutableStateOf(false) }

        if (client is RemoteClient) {
            val remoteClient = client as RemoteClient
            LaunchedEffect(remoteClient.host, remoteClient.port) {
                if (!client.connected)
                    remoteClient.connect()
            }
        }

        LaunchedEffect(client.self) {
            if (client.self == null)
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


                    useWindow {
                        useWindow {
                            with(fileState) {
                                dialog()
                            }
                        }
                    }

                    useColumn {
                        currentContact?.let {
                            useColumn {
                                ChatView(client, currentContact!!)
                            }
                        }
                    }
                }
            }

            if (client.self == null && client is RemoteClient) {
                val remoteClient = client as RemoteClient
                DefaultView(
                    onCloseRequest = ::exitApplication,
                    state = rememberCenterWindowState(DpSize(600.dp, Dp.Unspecified)),
                    title = "login.title".l
                ) {
                    LoginView(remoteClient, {
                        showSettings = true
                    }, {
                        showRegister = true
                    })
                }
            } else if (!showTray)
                DefaultView(
                    onCloseRequest = { showTray = true },
                    state = rememberCenterWindowState(DpSize(325.dp, 720.dp)),
                    title = "title".l
                ) {
                    MainView(client) {
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

            if (client is RemoteClient) {
                val remoteClient = client as RemoteClient
                if (showRegister) {
                    SurfaceView(
                        onCloseRequest = { showRegister = false },
                        state = rememberCenterWindowState(DpSize(600.dp, Dp.Unspecified)),
                        title = "register.title".l
                    ) {
                        RegisterView(remoteClient) {
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
                        SettingsView(remoteClient.host, remoteClient.port) { host, port ->
                            showSettings = false
                            if (remoteClient.host != host || remoteClient.port != port)
                                remoteClient.disconnect()
                            remoteClient.host = host
                            remoteClient.port = port
                        }
                    }
                }
            }
        }
    }

    println("Saving configuration...")
    configuration.save()

    localServer?.close()
    client.close()
    saveLogFile()
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}

internal fun saveLogFile() {
    try {
        val latest = File(configDir, "logs/latest.log")
        if (latest.exists()) {
            val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())
            val target = File(configDir, "logs/$name.log")
            Files.copy(latest, target)
            val gzipOutputStream = GZIPOutputStream(
                FileOutputStream(
                    File(
                        configDir,
                        "logs/$name.gz"
                    )
                )
            )
            gzipOutputStream.write(FileInputStream(target))
            gzipOutputStream.finish()
            gzipOutputStream.close()
            // ignore if failed
            target.delete()
        }
    } catch (ignored: IOException) {
    }
}

internal fun OutputStream.write(inputStream: InputStream) {
    val buffer = ByteArray(1024000)
    var length: Int
    while ((inputStream.read(buffer).also { length = it }) > 0) this.write(buffer, 0, length)
    inputStream.close()
}
