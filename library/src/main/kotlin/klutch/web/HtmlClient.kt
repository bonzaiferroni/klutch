package klutch.web

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.netty.handler.codec.compression.StandardCompressionOptions.deflate
import io.netty.handler.codec.compression.StandardCompressionOptions.gzip
import kabinet.console.globalConsole

private val console = globalConsole.getHandle(HtmlClient::class)

class HtmlClient(
    private val client: HttpClient = ktorHtmlClient,
    private val reader: HtmlReader = HtmlReader()
) {

    suspend fun readUrl(url: String) = fetch(url)?.takeIf { it.status == HttpStatusCode.OK }?.bodyAsText()
        ?.let { readHtml(url, it) }

    fun readHtml(url: String, html: String) = parseHtml(html).let { reader.read(url, it) }

    fun parseHtml(html: String): Document {
        return Ksoup.parse(html = html)
    }

    suspend fun fetch(url: String): HttpResponse? {
        return try {
            client.get(url)
        } catch (e: Exception) {
            console.logError(e.message.toString())
            null
        }
    }
}

private val ktorHtmlClient = HttpClient(CIO) {
    defaultRequest {
        headers {
            set(HttpHeaders.UserAgent, chromeLinuxAgent)
            extraHeaders.forEach { (key, value) ->
                set(key, value)
            }
            set(HttpHeaders.AcceptEncoding, "gzip, deflate")
        }
    }
    install(ContentEncoding) {
        gzip()
        deflate()
    }
    install(HttpSend) {
        maxSendCount = 40
    }
//        engine { // Apache config
//            followRedirects = true
//            socketTimeout = 30_000
//            connectTimeout = 30_000
//            connectionRequestTimeout = 30_000
//            customizeClient {
//                setMaxConnTotal(1000)
//                setMaxConnPerRoute(100)
//            }
//            customizeRequest {
//                // TODO: request transformations
//            }
//        }
    engine {
        requestTimeout = 30_000 // Timeout in milliseconds (30 seconds here)
    }
    HttpResponseValidator {
        // intercept exceptions (timeouts, DNS, etc.) before Ktor logs 'em
        handleResponseExceptionWithRequest { cause, _ ->
            // do yer own logging here; avoid println to keep silence
            console.logError(cause.message ?: "ktor error")
        }
    }
}

val chromeLinuxAgent =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
val extraHeaders = mutableMapOf(
    "priority" to "u=0, i",
    "dnt" to "1",
    "accept-language" to "en-US,en;q=0.9",
    "accept-encoding" to "gzip, deflate, br, zstd",
    "sec-ch-ua" to "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"",
    "sec-ch-ua-platform" to "\"Linux\"",
    "sec-ch-ua-mobile" to "?0",
    "sec-fetch-dest" to "document",
    "sec-fetch-mode" to "navigate",
    "sec-fetch-user" to "?1",
    "cache-control" to "max-age=0",
    "upgrade-insecure-requests" to "1"
)
