package top.focess.netdesign.config

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowScope
import top.focess.util.yaml.YamlConfiguration
import java.io.FileInputStream

class LangFile(filename: String) {

    private val configuration : YamlConfiguration = YamlConfiguration.load(try {
        FileInputStream(filename)
    } catch (e : Exception) {
        javaClass.classLoader.getResourceAsStream(filename)
    }
    );

    fun get(key: String) : String {
        val keys = key.split(".");
        var configuration : YamlConfiguration = this.configuration
        for (i in 0 until keys.size - 1)
            configuration = configuration.getSection(keys[i])
        return try { configuration.get(keys[keys.size - 1])?: key } catch (e : Exception) { key }
    }

    fun get(key: String, vararg args: Any) : String {
        return String.format(get(key), *args)
    }

    companion object {
        @Composable
        fun createLandScope(_langFile: LangFile, block: @Composable() (LangScope.() -> Unit)) {
            var langFile by remember { mutableStateOf(_langFile) }

            LangScope(langFile) {
                langFile = it
            }.block()
        }

    }

    open class LangScope(langFile: LangFile, val setLangFile: (langFile : LangFile) -> Unit) {

        var langFile: LangFile = langFile
            set(value) = setLangFile(value)

        fun String.l(vararg args: Any) : String {
            return String.format(langFile.get(this), *args)
        }

        val String.l: String
            get() = langFile.get(this)

        @Composable
        fun ColumnScope.useColumn(block: @Composable ColumnLangScope.() -> Unit) {
            ColumnLangScope(this, langFile, setLangFile).block()
        }

        @Composable
        fun RowScope.useRow(block: @Composable RowLangScope.() -> Unit) {
            RowLangScope(this, langFile, setLangFile).block()
        }

        @Composable
        fun FrameWindowScope.useWindow(block: @Composable WindowLangScope.() -> Unit) {
            WindowLangScope(this, langFile, setLangFile).block()
        }
    }

    class ColumnLangScope(private val columnScope: ColumnScope, langFile: LangFile, setLangFile: (langFile : LangFile) -> Unit) : LangScope(langFile, setLangFile) {

        @Composable
        fun column(block: @Composable ColumnScope.() -> Unit) {
            columnScope.block()
        }
    }

    class RowLangScope(private val rowScope: RowScope, langFile: LangFile, setLangFile: (langFile : LangFile) -> Unit) : LangScope(langFile, setLangFile) {

        @Composable
        fun row(block: @Composable RowScope.() -> Unit) {
            rowScope.block()
        }
    }

    class WindowLangScope(private val windowScope: FrameWindowScope, langFile: LangFile, setLangFile: (langFile : LangFile) -> Unit) : LangScope(langFile, setLangFile) {

        @Composable
        fun window(block: @Composable FrameWindowScope.() -> Unit) {
            windowScope.block()
        }
    }
}