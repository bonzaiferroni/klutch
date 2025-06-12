package klutch.clients

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject

suspend fun GeminiClient.generateImage(
    text: String
): String? {
    val response = tryRequest {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(
                            text = text
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseModalities = listOf("TEXT", "IMAGE")
            )
        )
        GeminiApiRequest("generateContent", request, "gemini-2.0-flash-preview-image-generation")
    }

    if (response?.status == HttpStatusCode.OK) {
        return response.body<GeminiResponse>().candidates.firstOrNull()?.content?.parts
            ?.firstOrNull() { it.inlineData != null }?.inlineData?.data
    } else {
        return null
    }
}

// curl -s -X POST \
//  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-preview-image-generation:generateContent?key=$GEMINI_API_KEY" \
//  -H "Content-Type: application/json" \
//  -d '{
//    "contents": [{
//      "parts": [
//        {"text": "Hi, can you create a 3d rendered image of a pig with wings and a top hat flying over a happy futuristic scifi city with lots of greenery?"}
//      ]
//    }],
//    "generationConfig":{"responseModalities":["TEXT","IMAGE"]}
//  }' \
//  | grep -o '"data": "[^"]*"' \
//  | cut -d'"' -f4 \
//  | base64 --decode > gemini-native-image.png