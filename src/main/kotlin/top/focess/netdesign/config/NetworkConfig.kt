package top.focess.netdesign.config

import java.io.FileReader
import java.util.*

val properties = Properties().apply {
    load(FileReader("gradle.properties"))
}

object NetworkConfig {

    val DEFAULT_SERVER_HOST = properties.getProperty("server.host")

    val DEFAULT_SERVER_PORT = properties.getProperty("server.port")!!.toInt()

}