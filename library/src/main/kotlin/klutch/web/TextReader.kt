package klutch.web

import kabinet.web.Url
import kabinet.web.fromHrefOrNull

class TextReader {
    fun read(title: String, url: Url, text: String): WebDocument {
        val contents = mutableListOf<WebContent>()
        val lines = text.split("\n")
        val buffer = StringBuilder()
        var wordCount = 0
        fun addAndClearBuffer() {
            if (buffer.isNotEmpty()) {
                val rawContent = buffer.toString()
                val (content, links) = extractLinks(rawContent)
                contents.add(WebContent(content, links))
                buffer.clear()
            }
        }
        for (line in lines) {
            val isPossibleNewChunk = line.isNotEmpty() && (line[0].isLetter() || line[0] == '#')
            if (isPossibleNewChunk && buffer.length >= CHUNK_SIZE) {
                addAndClearBuffer()
            }
            buffer.appendLine(line)
            wordCount += line.split(" ").filter { it.isNotEmpty() }.size
        }
        addAndClearBuffer()
        return WebDocument(
            title = title,
            url = url,
            contents = contents,
            wordCount = wordCount
        )
    }
}

fun extractLinks(markdown: String): Pair<String, List<WebLink>> {
    // [text](url) but not ![image](...)
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

const val CHUNK_SIZE = 500