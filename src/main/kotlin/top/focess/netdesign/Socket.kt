package top.focess.netdesign


class Socket {

    var host: String = "localhost"
    var port: Int = 34952

    companion object {
        fun getSocket() = Socket()
    }
}