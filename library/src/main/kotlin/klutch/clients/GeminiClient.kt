package klutch.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import klutch.log.LogLevel
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class GeminiClient(
    val limitedToken: String,
    val unlimitedToken: String? = null,
    val model: String = "gemini-1.5-flash",
    val client: HttpClient = ktorClient,
    val logMessage: (String, LogLevel, String) -> Unit
) {
    var unlimitedUntil: Instant = Instant.DISTANT_PAST
    var restingUntil: Instant = Instant.DISTANT_PAST
}

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiRequestText>,
)

@Serializable
data class GeminiRequestText(
    val text: String
)

