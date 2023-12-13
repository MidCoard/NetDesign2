package top.focess.netdesign.server

val EMPTY_MESSAGE = Message(-1, -1, -1, -1, TextMessageContent(""), -1)

data class Message(val id: Int, val from: Int, val to: Int, val internalId: Int, val content: MessageContent, val timestamp: Int)

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

abstract class RawMessageContent(val type: MessageType) {

    abstract fun toMessageContent() : MessageContent
}

data class RawTextMessageContent(val text: String) : RawMessageContent(MessageType.TEXT) {
    override fun toMessageContent() = TextMessageContent(text)
}

abstract class ARawFileMessageContent(val file: File, messageType: MessageType) : RawMessageContent(messageType) {
    override fun toMessageContent() = FileMessageContent("")
}

class RawFileMessageContent(file: File) : ARawFileMessageContent(file, MessageType.FILE)

class RawImageMessageContent(image: File) : ARawFileMessageContent(image, MessageType.IMAGE)

data class File(val filename: String, val data: ByteArray)