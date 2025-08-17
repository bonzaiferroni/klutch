package klutch.web

import com.fleeksoft.ksoup.nodes.Document
import klutch.utils.tryParseInstantOrNull

fun Document.readMetaContent(vararg propertyValues: String) = propertyValues.firstNotNullOfOrNull {
    this.selectFirst("meta[property=\"$it\"]")?.attribute("content")?.value
        ?: this.selectFirst("meta[name=\"$it\"]")?.attribute("content")?.value
}

fun Document.readCannonHref() = this.readMetaContent("url", "og:url", "twitter:url")
fun Document.readHeadline() = this.readMetaContent("title", "og:title", "twitter:title")
    ?.replace("\n", " ")?.take(100)
fun Document.readDescription() = this.readMetaContent("description", "og:description", "twitter:description")
fun Document.readImageUrl() = this.readMetaContent("image", "og:image", "twitter:image")
fun Document.readHostName() = this.readMetaContent("site", "og:site_name", "twitter:site")
fun Document.readType() = this.readMetaContent("type", "og:type")
fun Document.readAuthor() = this.readMetaContent("author", "article:author", "og:article:author")
fun Document.readPublishedAt() = this.readMetaContent("date", "article:published_time")?.tryParseInstantOrNull()
fun Document.readModifiedAt() = this.readMetaContent("last-modified", "article:modified_time")?.tryParseInstantOrNull()
fun Document.readLanguage() = this.readMetaContent("language", "og:locale")
    ?: this.selectFirst("html")?.attribute("lang")?.value

fun Document.parseNewsArticle(): MetaNewsArticle? {
    val innerHtml = this.selectFirst("script#json-schema")?.html() ?: this.scanTagsForNewsArticle() ?: return null
    val json = innerHtml.trimCData()
//    json.cacheResource(cacheId.core, "json", "news_article_raw")
    try {
        val article = json.decodeNewsArticle()
//        article?.cacheSerializable(cacheId.core, "news_article_parsed")
        return article // return
    } catch (e: Exception) {
        // json.cacheResource(cacheId.core, "json", "news_article_error")
        return null
    }
}

private fun Document.scanTagsForNewsArticle(): String? {
    val head = this.selectFirst("head") ?: return null
    for (element in head.children()) {
        if (element.tag.tagName == "script" && element.html().contains("\"NewsArticle\"")) {
            return element.html()
        }
    }
    return null
}

private fun String.trimCData() = this
    .removePrefix("//<![CDATA[")
    .removeSuffix("//]]>")
    .trim()