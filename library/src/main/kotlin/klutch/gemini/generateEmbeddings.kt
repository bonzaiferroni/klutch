package klutch.gemini

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import klutch.log.LogLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

suspend fun GeminiClient.generateEmbedding(
    text: String,
    dimensions: Int = 768,
    taskType: EmbeddingTaskType = EmbeddingTaskType.SEMANTIC_SIMILARITY,
): FloatArray? {
    val response = tryRequest {
        val request = GeminiEmbeddingRequest(
            content = GeminiContent(parts = listOf(GeminiPart(text))),
//            embeddingConfig = GeminiEmbeddingConfig(
//
//            ),
            taskType = taskType,
            outputDimensionality = dimensions
        )
        GeminiApiRequest("embedContent", request, "gemini-embed-001")
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
//    @SerialName("embedding_config")
//    val embeddingConfig: GeminiEmbeddingConfig? = null,
    @SerialName("task_type")
    val taskType: EmbeddingTaskType? = null,
    @SerialName("output_dimensionality")
    val outputDimensionality: Int? = null
)

@Serializable
data class GeminiEmbeddingConfig(
    @SerialName("task_type")
    val taskType: EmbeddingTaskType? = null,
    @SerialName("output_dimensionality")
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