package klutch.gemini

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kabinet.clients.GeminiMessage
import kabinet.clients.GeminiRole

suspend fun GeminiClient.generateText(
    contents: List<GeminiContent>
): String? {
    val response = tryRequest {
        val request = GeminiRequest(contents = contents)
        GeminiApiRequest("generateContent", request)
    }

    if (response?.status == HttpStatusCode.OK) {
        return response.body<GeminiResponse>().candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
    } else {
        return null
    }
}

suspend fun GeminiClient.generateTextFromMessages(
    messages: List<GeminiMessage>
) = generateText(messages.map { it.toGeminiContent()} )

suspend fun GeminiClient.generateTextFromParts(
    vararg parts: String
) = generateText(parts.map { GeminiContent("user", listOf(GeminiPart(it))) })

fun GeminiMessage.toGeminiContent() = GeminiContent(
    role = when (role) {
        GeminiRole.User -> "user"
        GeminiRole.Assistant -> "model"
    },
    parts = listOf(GeminiPart(message))
)