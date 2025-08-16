package klutch.html

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element

class HtmlReader {

    suspend fun read(
        url: String,
        document: Document,
        isFresh: (suspend (String) -> Boolean)? = null,
    ): DocumentContent {
        val docUrl = url.toUrl()
        val newsArticle = document.parseNewsArticle()

        var wordCount = 0
        var characterCount = 0
        val stack = mutableListOf<Element>()
        stack.addAll(document.children().reversed())
        val dropped = mutableListOf<Element>()
        val contents = mutableListOf<String>()
        val urls = mutableListOf<DocumentLink>()

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

            val paragraphs = element.text().split('\n')
            for (content in paragraphs) {
                if (content.length > 2000) continue
                if (isNotArticleParagraph(content)) continue
                if (!(isFresh?.let { it(content) } ?: true)) continue
                contents.add(content)
                characterCount += content.length
            }

            for (anchorElement in element.getElementsByTag("a")) {
                val href = anchorElement.attribute("href")?.value ?: continue
                val text = anchorElement.text().takeIf { it.isNotBlank() } ?: continue
                if (isHrefLikelyAd(href)) continue
                if (isHrefOtherProtocol(href)) continue
                if (isHrefRelative(href)) continue
                val url = href.toUrlOrNull() ?: continue
                if (urls.any { it.url.href == url.href }) continue
                val link = DocumentLink(url, text)
                urls.add(link)
            }
        }

        val title = document.readHeadline() ?: newsArticle?.headline ?: document.title()

        return DocumentContent(
            title = title,
            url = docUrl,
            paragraphs = contents,
            links = urls
        )
    }
}

data class DocumentContent(
    val title: String,
    val url: Url,
    val paragraphs: List<String>,
    val links: List<DocumentLink>,
) {
    fun toMarkdown() = "# $title\n\n" +
            "${paragraphs.joinToString("\n\n")}\n\n" +
            links.joinToString("\n") { "* $[${it.text}](${it.url.href})" }.let {

            }
}

data class DocumentLink(
    val url: Url,
    val text: String,
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

