package klutch.web

import com.fleeksoft.ksoup.Ksoup
import kabinet.web.Url

class HnReader {
    fun read(title: String?, url: Url, html: String): WebDocument {

        val text = Ksoup.parse(html = html).text()

        return WebDocument(
            title = title,
            url = url,
            contents = listOf(WebContent(text)),
            wordCount = 0,
        )
    }
}