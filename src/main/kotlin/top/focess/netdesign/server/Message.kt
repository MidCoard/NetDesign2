package top.focess.netdesign.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.swing.Spring.height


val EMPTY_MESSAGE = Message(-1, -1, -1, -1, TextMessageContent(""), -1)

data class Message(val id: Int, val from: Int, val to: Int, val _internalId: Int, val content: MessageContent, val timestamp: Int) {
    var internalId by mutableStateOf(_internalId)
}

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
}

abstract class MessageContent(val type: MessageType, val data: String)
data class TextMessageContent(val text: String) : MessageContent(MessageType.TEXT, text)
abstract class SpecialMessageContent(type: MessageType, id: String) : MessageContent(type, id)
class ImageMessageContent(id: String) : SpecialMessageContent(MessageType.IMAGE, id)
class FileMessageContent(id: String) : SpecialMessageContent(MessageType.FILE, id)

abstract class RawMessageContent {

    abstract fun toMessageContent() : MessageContent
}

data class RawTextMessageContent(var text: String) : RawMessageContent() {
    override fun toMessageContent() = TextMessageContent(text)
}

open class RawFileMessageContent(var file: File) : RawMessageContent() {
    override fun toMessageContent() : MessageContent = FileMessageContent("")

}
class RawImageMessageContent(val image: Painter) : @Composable RawFileMessageContent(image.toFile()) {

    override fun toMessageContent() = ImageMessageContent("")
}

class RawRichMessageContent(vararg rawMessageContents: RawMessageContent) : RawMessageContent() {

    val rawMessageContents = rawMessageContents.toList()
    override fun toMessageContent() = throw UnsupportedOperationException()
}

val EMPTY_FILE = File("", ByteArray(0))

data class File(val filename: String, val data: ByteArray)

internal fun Painter.toFile() = File("image", this.toBytes())

internal fun Painter.toBytes() = this.toAwtImage(Density(1f), LayoutDirection.Ltr).toBytes()

internal fun Image.toBytes() : ByteArray {
    val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    bufferedImage.graphics.drawImage(this, 0, 0, null)
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", outputStream)
    return outputStream.toByteArray()
}

val RawMessageContent.messageCount: Int
    get() = when(this) {
        is RawTextMessageContent -> 1
        is RawFileMessageContent -> 1
        is RawRichMessageContent -> rawMessageContents.size
        else -> 0
    }