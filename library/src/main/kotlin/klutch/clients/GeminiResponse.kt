package klutch.clients

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class GeminiResponse(
    val candidates: List<GeminiResponseCandidate>,
    val usageMetadata: GeminiUsage,
    val modelVersion: String,
)

@Serializable
data class GeminiResponseCandidate(
    val content: GeminiContent,
    val finishReason: String,
    val avgLogprobs: Float? = null,
)

@Serializable
data class GeminiUsage(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int,
    val promptTokensDetails: List<PromptTokenDetails>
)

@Serializable
data class PromptTokenDetails(
    val modality: String,
    val tokenCount: Int
)

@Serializable
data class GenerationConfig(
    val stopSequences: List<String>? = null,
    @SerialName("response_mime_type")
    val responseMimeType: String,
    @SerialName("response_schema")
    val responseSchema: JsonElement? = null,
    val responseModalities: List<Modality>? = null,
    val candidateCount: Int? = null,
    val maxOutputTokens: Int? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val seed: Int? = null,
    val presencePenalty: Double? = null,
    val frequencyPenalty: Double? = null,
    val responseLogprobs: Boolean? = null,
    val logprobs: Int? = null,
    val enableEnhancedCivicAnswers: Boolean? = null,
    val speechConfig: SpeechConfig? = null,
    val mediaResolution: MediaResolution? = null
)

@Serializable
data class Schema(
    val type: Type,
    val format: String,
    val description: String,
    val nullable: Boolean,
    val enum: List<String> = emptyList(),
    val maxItems: String,
    val minItems: String,
    val properties: Map<String, Schema> = emptyMap(),
    val required: List<String> = emptyList(),
    val propertyOrdering: List<String> = emptyList(),
    val items: Schema? = null
)

@Serializable
data class SpeechConfig(
    val voiceConfig: VoiceConfig
)


@Serializable
data class Modality(
    val voiceConfig: VoiceConfig
)

@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String
)

@Serializable
enum class MediaResolution {
    @SerialName("MEDIA_RESOLUTION_UNSPECIFIED") Unspecified,
    @SerialName("MEDIA_RESOLUTION_LOW") Low,
    @SerialName("MEDIA_RESOLUTION_MEDIUM") Medium,
    @SerialName("MEDIA_RESOLUTION_HIGH") High
}

@Serializable
enum class Type {
    @SerialName("TYPE_UNSPECIFIED") Unspecified,
    @SerialName("STRING") String,
    @SerialName("NUMBER") Number,
    @SerialName("INTEGER") Integer,
    @SerialName("BOOLEAN") Boolean,
    @SerialName("ARRAY") Array,
    @SerialName("OBJECT") Object
}

