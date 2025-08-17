package klutch.web

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

fun <T> JsonElement.toArray(
    convertObject: (JsonObject) -> T,
    convertPrimitive: (JsonPrimitive) -> T
): List<T>? {
    return when (this) {
        is JsonObject -> listOf(convertObject(this))
        is JsonPrimitive -> listOf(convertPrimitive(this))
        is JsonArray -> this.mapNotNull {
            when (it) {
                is JsonObject -> convertObject(it)
                is JsonPrimitive -> convertPrimitive(it)
                else -> null
            }
        }

        else -> null
    }
}

fun <T> JsonElement.toSingle(
    convertObject: (JsonObject) -> T,
    convertPrimitive: (JsonPrimitive) -> T
): T? {
    return when (this) {
        is JsonObject -> convertObject(this)
        is JsonPrimitive -> convertPrimitive(this)
        is JsonArray -> this.firstOrNull()?.let {
            when (it) {
                is JsonObject -> convertObject(it)
                is JsonPrimitive -> convertPrimitive(it)
                else -> null
            }
        }

        else -> null
    }
}

fun JsonElement.toStrings(convertObject: ((JsonObject) -> String)? = null) = when (this) {
    is JsonPrimitive -> listOf(this.content)
    is JsonArray -> this.mapNotNull { element ->
        when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> convertObject?.let { it(element) }
            else -> null
        } }
    is JsonObject -> convertObject?.let { listOf(it(this)) } ?: emptyList()
    else -> null
}

fun <T> JsonArray.findType(type: String, convert: (JsonObject) -> T): T? {
    return this.mapNotNull {
        it as? JsonObject
    }.firstOrNull {
        it["@type"]?.toString()?.contains(type) == true
    }?.let {
        convert(it)
    }
}

val JsonElement.primitiveOrNull get() = try {
    this.jsonPrimitive
} catch (e: IllegalArgumentException) {
    null
}