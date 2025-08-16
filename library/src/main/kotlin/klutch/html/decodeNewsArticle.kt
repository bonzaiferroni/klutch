package klutch.html

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

fun String.decodeNewsArticle(): MetaNewsArticle? {
    val element = runCatching { jsonDecoder.decodeFromString<JsonElement>(this) }.getOrNull() ?: return null
    return element.findNewsArticle()
}

fun JsonElement.findNewsArticle(): MetaNewsArticle? = when (this) {
    is JsonObject -> this.findNewsArticle()
    is JsonArray -> this.findNewsArticle()
    else -> null
}

fun JsonObject.findNewsArticle() = this.toNewsArticle()
    ?: this.values.firstNotNullOfOrNull { it.toNewsArticle() }
    ?: this.values.firstNotNullOfOrNull { it.findNewsArticle() }

fun JsonArray.findNewsArticle() = this.firstNotNullOfOrNull { it.toNewsArticle() }
    ?: this.firstNotNullOfOrNull { it.findNewsArticle() }

fun JsonElement.toNewsArticle() = if (this is JsonObject) this.toNewsArticle() else null

fun JsonObject.toNewsArticle(): MetaNewsArticle? {
    if (this["@type"]?.toString()?.contains("NewsArticle") != true) return null
    return MetaNewsArticle(
        headline = this["headline"]?.primitiveOrNull?.contentOrNull,
        dateline = this["dateline"]?.primitiveOrNull?.contentOrNull,
        datePublished = this["datePublished"]?.primitiveOrNull?.contentOrNull,
        dateModified = this["dateModified"]?.primitiveOrNull?.contentOrNull,
        author = this["author"]?.toArray(JsonObject::toAuthor) { NewsAuthor(name = it.toString()) },
        publisher = this["publisher"]?.toSingle(JsonObject::toPublisher) { Publisher(name = it.toString()) },
        articleBody = this["articleBody"]?.primitiveOrNull?.contentOrNull,
        articleSection = this["articleSection"]?.toStrings(),
        wordCount = this["wordCount"]?.primitiveOrNull?.intOrNull,
        keywords = this["keywords"]?.toStrings(),
        abstract = this["abstract"]?.primitiveOrNull?.contentOrNull,
        alternativeHeadline = this["alternativeHeadline"]?.primitiveOrNull?.contentOrNull,
        description = this["description"]?.primitiveOrNull?.contentOrNull,
        url = this["url"]?.primitiveOrNull?.contentOrNull,
        isAccessibleForFree = this["isAccessibleForFree"]?.primitiveOrNull?.booleanOrNull,
        text = this["text"]?.primitiveOrNull?.contentOrNull,
        thumbnailUrl = this["thumbnailUrl"]?.primitiveOrNull?.contentOrNull,
        image = this["image"]?.toArray(JsonObject::toImage) { Image(url = it.toString()) },
        inLanguage = this["inLanguage"]?.primitiveOrNull?.contentOrNull,
        commentCount = this["commentCount"]?.primitiveOrNull?.intOrNull
    )
}

fun JsonObject.toPublisher() = Publisher(
    name = this["name"]?.primitiveOrNull?.contentOrNull,
    logo = this["logo"]?.toSingle(JsonObject::toImage) { Image(url = it.toString()) }
)

fun JsonObject.toAuthor() = NewsAuthor(
    name = this["name"]?.primitiveOrNull?.contentOrNull,
    url = this["url"]?.primitiveOrNull?.contentOrNull,
    email = this["email"]?.primitiveOrNull?.contentOrNull,
    sameAs = this["sameAs"]?.toStrings(),
    image = this["image"]?.toSingle(JsonObject::toImage) { Image(url = it.toString()) }
)

fun JsonObject.toImage() = Image(
    width = this["width"]?.toSingle(JsonObject::toQuantInt) { it.intOrNull },
    height = this["height"]?.toSingle(JsonObject::toQuantInt) { it.intOrNull },
    url = this["url"]?.primitiveOrNull?.contentOrNull,
    caption = this["caption"]?.primitiveOrNull?.contentOrNull,
    creditText = this["creditText"]?.primitiveOrNull?.contentOrNull,
)

fun JsonObject.toQuantInt() = this["value"]?.primitiveOrNull?.intOrNull

val jsonDecoder = Json { ignoreUnknownKeys = true}