package top.focess.netdesign.config

import top.focess.util.yaml.YamlConfiguration
import java.io.File


private fun loadAsMap(file: File) = YamlConfiguration.loadFile(file).values
class FileConfiguration(val file: File) : YamlConfiguration(loadAsMap(file)) {

    fun save() {
        super.save(this.file)
    }

    companion object {
        fun loadFile(file: File) = FileConfiguration(file)
    }
}