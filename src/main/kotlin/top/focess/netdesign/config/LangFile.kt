package top.focess.netdesign.config

import androidx.compose.runtime.Composable
import top.focess.util.yaml.YamlConfiguration
import java.io.FileInputStream

class LangFile(filename: String) {

    private val configuration : YamlConfiguration = YamlConfiguration.load(try {
        FileInputStream(filename)
    } catch (e : Exception) {
        javaClass.classLoader.getResourceAsStream(filename)
    }
    );

    @Composable
    fun get(key: String) : String {
        val keys = key.split(".");
        var configuration : YamlConfiguration = this.configuration
        for (i in 0 until keys.size - 1)
            configuration = configuration.getSection(keys[i])
        return configuration.get(keys[keys.size - 1])?: key
    }
    @Composable
    fun get(key: String, vararg args: Any) : String {
        return String.format(get(key), *args)
    }

    companion object {
        fun createLandScope(langFile: LangFile, block: LangScope.() -> Unit) {
            LangScope(langFile).block()
        }
    }

    class LangScope(private val langFile: LangFile) {

        @Composable
        fun String.l(vararg args: Any) : String {
            return String.format(langFile.get(this), *args)
        }


        val String.l: String
            @Composable
            get() = langFile.get(this)

    }
}