package klutch.web

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kabinet.web.Url
import kabinet.web.toUrl
import kabinet.web.toUrlOrNull
import kotlinx.datetime.Instant
import java.io.File

class HtmlReader {
    fun read(
        url: String,
        document: Document,
    ): WebDocument {
        val docUrl = url.toUrl()
        val newsArticle = document.parseNewsArticle()

        var wordCount = 0
        var characterCount = 0
        val stack = mutableListOf<Element>()
        stack.addAll(document.children().reversed())
        val dropped = mutableListOf<Element>()
        val contents = mutableListOf<WebContent>()

        while (stack.isNotEmpty()) {
            val element = stack.removeLastOrNull() ?: break
            val tag = element.tag.tagName

            if (tag in lassoTags) {
                if (tag == "article") {
                    dropped.addAll(stack)
                }
                stack.clear()
            }
            if (element.hasChildNodes() && tag !in notParentTags) {
                stack.addAll(element.children().reversed())
                continue
            }

            if (stack.isEmpty() && wordCount == 0) {
                stack.addAll(dropped)
                dropped.clear()
            }

            if (tag !in contentTags) continue

            val content = element.text().trim()
            if (content.isEmpty() || content.length > 4000) continue
            if (isNotArticleParagraph(content)) continue

            val links = mutableListOf<WebLink>()
            for (anchorElement in element.getElementsByTag("a")) {
                val href = anchorElement.attribute("href")?.value ?: continue
                val text = anchorElement.text().takeIf { it.isNotBlank() } ?: continue
                if (isHrefLikelyAd(href)) continue
                if (isHrefOtherProtocol(href)) continue
                if (isHrefRelative(href)) continue
                val url = href.toUrlOrNull() ?: continue
                if (contents.any { content -> content.links.any { it.url.href == url.href } }) continue
                val startIndex = if (content.countMatches(text) == 1) content.indexOf(text).takeIf { it > 0 } else null
                val link = WebLink(url, text, startIndex)
                links.add(link)
            }

            contents.add(WebContent(
                text = content,
                links = links
            ))
            characterCount += content.length
            wordCount += content.split(" ").filter { it.isNotBlank() }.size
        }

        val title = document.readHeadline() ?: newsArticle?.headline ?: document.title()
        val hostName = newsArticle?.publisher?.name ?: document.readHostName()

        return WebDocument(
            title = title,
            url = docUrl,
            wordCount = wordCount,
            contents = contents,
            publisherName = hostName
        )
    }
}

data class WebDocument(
    val url: Url,
    val wordCount: Int,
    val contents: List<WebContent>,
    val title: String? = null,
    val publisherName: String? = null,
    val publishedAt: Instant? = null,
) {
//    fun toMarkdown() = "# $title\n\n" +
//            "${paragraphs.joinToString("\n\n")}\n\n" +
//            contents.joinToString("\n") { "* $[${it.text}](${it.url.href})" }.let {
//
//            }
}

data class WebContent(
    val text: String,
    val links: List<WebLink> = emptyList(),
)

data class WebLink(
    val url: Url,
    val text: String,
    val startIndex: Int?,
)

private val contentTags = setOf("p", "li", "span", "blockquote")
private val notParentTags = setOf(
    "p", "h1", "h2", "h3", "h4", "h5", "h6",
    "span", "a", "img", "nav", "head", "header", "footer",
    "form", "input", "button", "label", "textarea",
    "table", "thead", "tbody", "tr", "td", "th",
    "figure", "figcaption", "iframe", "aside",
    "details", "summary", "fieldset", "legend",
    "script", "style", "link", "meta", "svg", "embed"
)
private val lassoTags = setOf("body", "main", "article")

private val headingTags = setOf("h1", "h2", "h3", "h4", "h5", "h6")

private val sentenceEnders = setOf('.', '?', '!')

fun Element.isHeading() = tag.tagName in headingTags

fun isHrefLikelyAd(href: String) = href.length > MAX_URL_CHARS
fun isHrefOtherProtocol(href: String) = href.contains("mailto:")
fun isHrefRelative(href: String) = !href.startsWith("http")

fun isNotArticleParagraph(text: String) = text.isBlank()
        || text.first().isLowerCase()
        || text.toCharArray().none { sentenceEnders.contains(it) }

const val MAX_URL_CHARS = 400

private fun String.countMatches(sub: String): Int = windowed(sub.length) { it == sub }.count { it }

