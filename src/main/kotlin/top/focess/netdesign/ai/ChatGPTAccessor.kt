package top.focess.netdesign.ai

import top.focess.netdesign.server.*
import top.focess.netdesign.server.insertMessage
import top.focess.netdesign.server.packet.ContactMessageListRequestPacket
import top.focess.scheduler.ThreadPoolScheduler
import top.focess.util.json.JSONList
import top.focess.util.json.JSONObject
import top.focess.util.network.NetworkHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChatGPTAccessor(val apiKey: String, val model: ChatGPTModel) {

    private val messageHandlers = ConcurrentHashMap<Int, ChatGPTMessageHandler>()
    private val scheduler = ThreadPoolScheduler("ChatGPTAccessor", 10)
    private val networkHandler = NetworkHandler()

    val id = -1

    fun SingleServer.sendMessage(id: Int, messageContent: MessageContent) {
        if (messageContent.type != model.messageType) {
            val message = insertMessage(this@ChatGPTAccessor.id, id, "Message type not match.", MessageType.TEXT)
            sendChannelPacket(id, ContactMessageListRequestPacket(message))
        } else {
            val handler = messageHandlers.getOrPut(id) { ChatGPTMessageHandler() }
            scheduler.run {
                synchronized(handler) {
                    handler.addUserMessage(messageContent)
                    try {
                        if (model.messageType == MessageType.TEXT) {
                            val response = networkHandler.post(
                                "https://api.openai.com/v1/chat/completions", mapOf(
                                    "model" to model.model,
                                    "messages" to handler.asRawChatGPTMessages(),
                                ), mapOf(
                                    "Authorization" to "Bearer $apiKey",
                                    "Content-Type" to "application/json"
                                ), NetworkHandler.JSON
                            )
                            val choices: JSONList = response.asJSON.getList("choices")
                            val choice: JSONObject = choices.getJSON(0)
                            val message: JSONObject = choice.getJSON("message")
                            val content: String = message["content"]
                            val messageResponse = TextMessageContent(content)
                            val insertedMessage = insertMessage(this@ChatGPTAccessor.id, id, content, MessageType.TEXT)
                            sendChannelPacket(id, ContactMessageListRequestPacket(insertedMessage))
                            handler.addMessage(messageResponse)
                        } else {
                            val insertedMessage = insertMessage(this@ChatGPTAccessor.id, id, "Not supported.", MessageType.TEXT)
                            sendChannelPacket(id, ContactMessageListRequestPacket(insertedMessage))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handler.removeLastMessage()
                    }
                }
            }

        }
    }
}

private class ChatGPTMessageHandler {

    private val messages : MutableList<ChatGPTMessage> = mutableListOf()
    init {
        messages.add(ChatGPTMessage(ChatGPTRole.SYSTEM, TextMessageContent("You are chatting online with your friend.")))
    }
    fun addUserMessage(messageContent: MessageContent) {
        messages.add(ChatGPTMessage(ChatGPTRole.USER, messageContent))
    }

    fun addMessage(messageContent: MessageContent) {
        messages.add(ChatGPTMessage(ChatGPTRole.ASSISTANT, messageContent))
        if (messages.size > 50)
            messages.clear()
    }

    fun asRawChatGPTMessages(): List<Map<String, String>> {
        return messages.map {
            mapOf(
                "role" to it.role.name.lowercase(Locale.getDefault()),
                "content" to it.content.data
            )
        }
    }

    fun removeLastMessage() {
        messages.removeLast()
    }

    data class ChatGPTMessage(val role: ChatGPTRole, val content: MessageContent)
    enum class ChatGPTRole {
        SYSTEM, USER, ASSISTANT
    }
}

enum class ChatGPTModel(val model: String, val messageType: MessageType = MessageType.TEXT) {
    GPT4_TURBO("gpt-4-1106-preview"),
    GPT4_VISION_TURBO("gpt-4-vision-preview"),
    GPT3_5("gpt-3.5-turbo-1106"),
    DALL_E_3("dall-e-3", MessageType.IMAGE);

    companion object {
        fun fromString(string: String) = entries.find { it.model == string }
    }

}