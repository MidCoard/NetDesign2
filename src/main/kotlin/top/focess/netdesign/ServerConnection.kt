package top.focess.netdesign

import top.focess.netdesign.server.Server


class ServerConnection {

    var host: String = "localhost"
    var port: Int = 34952

    companion object {
        fun getServerConnection() = ServerConnection()
    }

    fun getServer() : Server? {
        return null
    }
}