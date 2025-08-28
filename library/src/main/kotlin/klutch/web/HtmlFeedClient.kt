package klutch.web

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kabinet.console.globalConsole
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

private val console = globalConsole.getHandle(HtmlFeedClient::class)

class HtmlFeedClient(
    val feedUrl: String,
    private val storyQuery: String,
    private val provideAuthor: (Element) -> String?,
    private val provideIdentifier: (Element) -> String?,
    private val provideScore: (Element) -> Int?,
    private val provideTime: (Element) -> Instant?,
    private val provideTitle: (Element) -> String?,
    private val provideCommentCount: (Element) -> Int?,
    private val provideUrl: (Element) -> String?,
    private val provideLink: (Element) -> String?,
    private val provideText: (Element) -> String?,
) {
    private val client: HttpClient = htmlFeedKtorClient

    suspend fun getItems(): List<HtmlFeedItem>? {
        val html = runCatching { client.get(feedUrl).bodyAsText() }.getOrNull() ?: return null
        val document = Ksoup.parse(html)
        val stories = document.select(storyQuery)
        console.log("stories: ${stories.size}")
        return stories.mapNotNull { storyNode ->
            HtmlFeedItem(
                author = provideAuthor(storyNode),
                identifier = provideIdentifier(storyNode)?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null,
                score = provideScore(storyNode) ?: return@mapNotNull null,
                time = provideTime(storyNode) ?: return@mapNotNull null,
                title = provideTitle(storyNode)?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null,
                commentCount = provideCommentCount(storyNode) ?: 0,
                url = provideUrl(storyNode)?.takeIf { it.isNotEmpty() }?.let {
                    when {
                        it.contains("https://") -> it
                        else -> "$feedUrl$it"
                    }
                } ?: return@mapNotNull null,
                link = provideLink(storyNode)?.takeIf { it.isNotEmpty() },
                text = provideText(storyNode)?.takeIf { it.isNotEmpty() },
            )
        }
    }
}

internal val htmlFeedKtorClient = HttpClient(CIO) {
    engine {
        requestTimeout = 30_000 // Timeout in milliseconds (30 seconds here)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000 // Set request timeout
        connectTimeoutMillis = 30_000 // Set connection timeout
        socketTimeoutMillis = 30_000  // Set socket timeout
    }
}

@Serializable
data class HtmlFeedItem(
    val author: String?,
    val identifier: String,
    val score: Int,
    val time: Instant,
    val title: String,
    val commentCount: Int,
    val url: String,
    val link: String?,
    val text: String?,
)