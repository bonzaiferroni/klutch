package klutch.clients

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import klutch.log.LogLevel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

suspend inline fun <reified Received> GeminiClient.requestJson(
    maxAttempts: Int,
    vararg parts: String
): Received? {
    while (restingUntil > Clock.System.now()) {
        delay((restingUntil - Clock.System.now()))
    }
    restingUntil = Clock.System.now() + 6.seconds

    for (attempt in 0 until maxAttempts) {

        val isUnlimited = unlimitedToken != null && Clock.System.now() < unlimitedUntil
        if (isUnlimited) logMessage("GeminiClient", LogLevel.INFO, "Unlimited Token Used")

        val token = when {
            isUnlimited -> unlimitedToken
            else -> limitedToken
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$token"
            val request = GeminiRequest(
                contents = parts.map { GeminiContent("user", listOf(GeminiRequestText(it))) },
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    responseSchema = generateJsonSchema<Received>()
                )
            )

            val ktorRequest = HttpRequestBuilder().apply {
                method = HttpMethod.Post
                url(url)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val response = client.request(ktorRequest)

            if (response.status == HttpStatusCode.OK) {
                return response.body<GeminiResponse>()
                    .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.let {
                        Json.decodeFromString(it)
                    }
            } else if (response.status == HttpStatusCode.TooManyRequests) {
                if (isUnlimited) {
                    restingUntil = Clock.System.now() + 1.hours
                    logMessage("GeminiClient", LogLevel.ERROR, "Rate limit reached on unlimited token, resting")
                } else {
                    unlimitedUntil = Clock.System.now() + 4.hours
                    logMessage("GeminiClient", LogLevel.ERROR, "Too many requests")
                }
            } else {
                logMessage(
                    "GeminiClient",
                    LogLevel.ERROR,
                    "attempt ${attempt + 1} failed:\n${response.body<JsonObject>()}"
                )
            }
        } catch (e: HttpRequestTimeoutException) {
            logMessage("GeminiClient", LogLevel.ERROR, "Request timed out")
        } catch (e: NoTransformationFoundException) {
            logMessage("GeminiClient", LogLevel.ERROR, "no transformation? 😕\n${e.message}")
        } catch (e: Exception) {
            logMessage("GeminiClient", LogLevel.ERROR, "requestJson exception:\n${e.message}")
        }
    }
    return null
}

inline fun <reified T> generateJsonSchema(): JsonElement {
    val descriptor = serializer<T>().descriptor
    return mapTypeToJson(descriptor)
}

@OptIn(ExperimentalSerializationApi::class)
fun objectToJson(descriptor: SerialDescriptor) = buildJsonObject {
    put("type", "OBJECT")
    put("properties", buildJsonObject {
        descriptor.elementDescriptors.forEachIndexed { index, childDescriptor ->
            put(descriptor.getElementName(index).toSnakeCase(), mapTypeToJson(childDescriptor))
        }
    })
}

@OptIn(ExperimentalSerializationApi::class)
fun primitiveToJson(type: String) = buildJsonObject {
    put("type", type)
}

@OptIn(ExperimentalSerializationApi::class)
fun arrayToJson(descriptor: SerialDescriptor) = buildJsonObject {
    put("type", "ARRAY")
    put("items", mapTypeToJson(descriptor.getElementDescriptor(0)))
}

@OptIn(ExperimentalSerializationApi::class)
fun mapTypeToJson(descriptor: SerialDescriptor): JsonObject = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> objectToJson(descriptor)
    StructureKind.LIST -> arrayToJson(descriptor)
    PrimitiveKind.STRING -> primitiveToJson("STRING")
    PrimitiveKind.INT, PrimitiveKind.LONG, PrimitiveKind.SHORT, PrimitiveKind.BYTE -> primitiveToJson("INTEGER")
    PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> primitiveToJson("NUMBER")
    PrimitiveKind.BOOLEAN -> primitiveToJson("BOOLEAN")
    else -> error("unknown type: ${descriptor.kind}")
}

fun String.toSnakeCase(): String = this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()