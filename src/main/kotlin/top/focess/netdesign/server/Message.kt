package top.focess.netdesign.server

data class Message(val id: Int, val from: Int, val to: Int, val internalId: Int, val content: MessageContent, val timestamp: Int)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
}

abstract class MessageContent(val type: MessageType, val data: String) {
}
data class TextMessageContent(val text: String) : MessageContent(MessageType.TEXT, text)
abstract class SpecialMessageContent(type: MessageType, id: String) : MessageContent(type, id)
class ImageMessageContent(id: String) : SpecialMessageContent(MessageType.IMAGE, id)
class FileMessageContent(id: String) : SpecialMessageContent(MessageType.FILE, id)