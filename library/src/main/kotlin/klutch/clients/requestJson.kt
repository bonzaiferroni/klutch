package klutch.clients

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
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


suspend inline fun <reified Received> GeminiClient.generateJson(
    vararg parts: String
): Received? {
    val response = tryRequest {
        val request = GeminiRequest(
            contents = parts.map { GeminiContent("user", listOf(GeminiRequestText(it))) },
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = generateJsonSchema<Received>()
            )
        )
        GeminiApiRequest("$model:generateContent", request)
    }

    if (response?.status == HttpStatusCode.OK) {
        return response.body<GeminiResponse>().candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.let {
            Json.decodeFromString(it)
        }
    } else {
        return null
    }
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