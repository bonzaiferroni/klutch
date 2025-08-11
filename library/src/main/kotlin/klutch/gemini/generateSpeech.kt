package klutch.gemini

import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kabinet.model.SpeechVoice

suspend fun GeminiClient.generateSpeech(
    text: String,
    theme: String?,
    voice: String?,
): String? {
    val response = tryRequest {
        val request = GeminiRequest(
            contents = listOf(GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "${theme ?: "Say"}: $text"
                    ))
            )),
            generationConfig = GenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(
                            voiceName = voice ?: SpeechVoice.Upbeat.apiName
                        )
                    )
                )
            )
        )
        GeminiApiRequest("generateContent", request, "gemini-2.5-flash-preview-tts")
    }

    if (response?.status == HttpStatusCode.OK) {
        val geminiResponse = response.body<GeminiResponse>()
        val data = geminiResponse.candidates.firstOrNull()?.content?.parts
            ?.firstOrNull() { it.inlineData != null}?.inlineData?.data
        if (data == null) {
            println("Missing data in speech generation:\n${geminiResponse}")
        }
        return data
    } else {
        return null
    }
}