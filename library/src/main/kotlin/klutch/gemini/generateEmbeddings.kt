package klutch.gemini

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import klutch.log.LogLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

suspend fun GeminiClient.generateEmbeddings(text: String): FloatArray? {
    val response = tryRequest {
        val request = GeminiEmbeddingRequest(
            content = GeminiContent(parts = listOf(GeminiPart(text))),
            taskType = EmbeddingTaskType.SEMANTIC_SIMILARITY
        )
        GeminiApiRequest("text-embedding-004:embedContent", request)
    }
    if (response?.status == HttpStatusCode.OK) {
        return response.body<GeminiEmbeddingResponse>()
            .embedding.values
    } else {
        logMessage(LogLevel.ERROR, "failed:\n${response?.body<JsonObject>()}")
    }
    return null
}

@Serializable
data class GeminiEmbeddingResponse(
    val embedding: ContentEmbedding
)

@Suppress("ArrayInDataClass")
@Serializable
data class ContentEmbedding(
    val values: FloatArray
)

@Serializable
data class GeminiEmbeddingRequest(
    val content: GeminiContent,
    val taskType: EmbeddingTaskType? = null,
    val title: String? = null,
    val outputDimensionality: Int? = null
)

enum class EmbeddingTaskType {
    TASK_TYPE_UNSPECIFIED,
    RETRIEVAL_QUERY,
    RETRIEVAL_DOCUMENT,
    SEMANTIC_SIMILARITY,
    CLASSIFICATION,
    CLUSTERING,
    QUESTION_ANSWERING,
    FACT_VERIFICATION
}