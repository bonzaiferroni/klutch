package klutch.clients

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject

suspend fun GeminiClient.generateSpeech(
    text: String
): String? {
    val response = tryRequest {
        val request = GeminiRequest(
            contents = listOf(GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "Say: $text"
                    ))
            )),
            generationConfig = GenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(
                            voiceName = "Kore"
                        )
                    )
                )
            )
        )
        GeminiApiRequest("generateContent", request, "gemini-2.5-pro-preview-tts")
    }

    if (response?.status == HttpStatusCode.OK) {
        return response.body<GeminiResponse>().candidates.firstOrNull()?.content?.parts
            ?.firstOrNull() { it.inlineData != null}?.inlineData?.data
    } else {
        return null
    }
}