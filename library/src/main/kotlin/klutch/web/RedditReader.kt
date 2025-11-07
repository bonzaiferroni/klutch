package klutch.web

import kabinet.clients.WebContent
import kabinet.clients.WebDocument
import kabinet.clients.WebLink
import kabinet.web.Url
import kabinet.web.fromHrefOrNull

class RedditReader {
    fun read(title: String?, url: Url, text: String): WebDocument {
        val contents = mutableListOf<WebContent>()
        val buffer = StringBuilder()
        var wordCount = 0
        fun addAndClearBuffer() {
            if (buffer.isNotEmpty()) {
                val rawContent = buffer.toString().removeSuffix("\n").prefixWithinBytesLimit()
                val (content, links) = extractLinks(rawContent)
                contents.add(WebContent(content, links))
                buffer.clear()
            }
        }
        val paragraphs = text.split("\n\n")
        for (paragraph in paragraphs) {
            val lines = paragraph.split("\n")
            for (line in lines) {
                if (line.isBlank()) continue
                val isPossibleNewChunk = line.isNotEmpty() && (line[0].isLetter() || line[0] == '#')
                if (isPossibleNewChunk && buffer.length >= CHUNK_SIZE) {
                    addAndClearBuffer()
                }
                buffer.appendLine(line)
                wordCount += line.split(" ").filter { it.isNotEmpty() }.size
            }
            addAndClearBuffer()
        }
        return WebDocument(
            title = title,
            url = url,
            contents = contents,
            wordCount = wordCount,
        )
    }
}

fun extractLinks(markdown: String): Pair<String, List<WebLink>> {
    // [label](url) but not ![image](...)
    val mdLinkRegex = Regex("""(?<!!)\[(.+?)]\((https?[^)\s]+)(?:\s+"[^"]*")?\)""")
    // Plaintext http(s) links
    val plainLinkRegex = Regex("""https?://\S+""")

    val links = mutableListOf<WebLink>()
    val out = StringBuilder()
    var last = 0

    // First pass: handle markdown links (strip markup)
    for (m in mdLinkRegex.findAll(markdown)) {
        out.append(markdown, last, m.range.first)

        val text = m.groupValues[1]
        val urlStr = m.groupValues[2]
        val start = out.length

        val url = Url.fromHrefOrNull(urlStr)
        if (url != null) {
            links += WebLink(url, text, start)
        }

        out.append(text)
        last = m.range.last + 1
    }
    if (last < markdown.length) out.append(markdown, last, markdown.length)

    val cleaned = out.toString()

    // Second pass: scan for plaintext http links, but don’t strip them
    for (m in plainLinkRegex.findAll(cleaned)) {
        val urlStr = m.value
        val start = m.range.first

        val url = Url.fromHrefOrNull(urlStr)
        if (url != null) {
            links += WebLink(url, urlStr, start)
        }
    }

    return cleaned to links
}

fun String.prefixWithinBytesLimit(limitBytes: Int = MAX_BTREE_LIMIT): String {
    val maxUnits = limitBytes / 3
    if (length <= maxUnits) return this
    var cut = maxUnits
    // avoid cutting inside a surrogate pair
    if (cut in 1 until length && this[cut].isLowSurrogate() && this[cut - 1].isHighSurrogate()) {
        cut -= 1
    }
    return substring(0, cut)
}

private fun Char.isHighSurrogate() = this in '\uD800'..'\uDBFF'
private fun Char.isLowSurrogate()  = this in '\uDC00'..'\uDFFF'

const val MAX_BTREE_LIMIT = 2704
const val CHUNK_SIZE = 500