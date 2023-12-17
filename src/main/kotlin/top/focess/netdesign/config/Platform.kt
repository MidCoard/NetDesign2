package top.focess.netdesign.config

enum class Platform {

    WINDOWS,

    MACOS,

    LINUX,

    UNKNOWN;

    companion object {

        val CURRENT_OS: Platform = when {
            System.getProperty("os.name").startsWith("Windows") -> WINDOWS
            System.getProperty("os.name").startsWith("Mac") -> MACOS
            System.getProperty("os.name").startsWith("Linux") -> LINUX
            else -> UNKNOWN
        }

    }
}