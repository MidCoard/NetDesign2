package top.focess.netdesign.server

data class Message(val id: Int, val from: Int, val to: Int, val content: MessageContent)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
}
abstract class MessageContent(val type: MessageType)
class TextMessageContent(val text: String) : MessageContent(MessageType.TEXT)
abstract class SpecialMessageContent(type: MessageType, id: Int) : MessageContent(type)
class ImageMessageContent(id: Int) : SpecialMessageContent(MessageType.IMAGE, id)
class FileMessageContent(id: Int) : SpecialMessageContent(MessageType.FILE, id)