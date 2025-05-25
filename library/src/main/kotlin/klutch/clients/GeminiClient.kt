package klutch.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import klutch.log.LogLevel
import kotlinx.serialization.Serializable

class GeminiClient(
    val token: String,
    val model: String = "gemini-1.5-flash",
    val client: HttpClient = ktorClient,
    val logMessage: (LogLevel, String) -> Unit
) {
    suspend inline fun <reified T> tryRequest(requestBlock: suspend () -> GeminiApiRequest<T>): HttpResponse? {
        try {
            val request = requestBlock()
            val model = request.model ?: model
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:${request.method}?key=$token"
            val ktorRequest = HttpRequestBuilder().apply {
                method = HttpMethod.Post
                url(url)
                contentType(ContentType.Application.Json)
                setBody(request.body)
            }
            return client.request(ktorRequest)
        } catch (e: HttpRequestTimeoutException) {
            logMessage(LogLevel.ERROR, "Request timed out")
        } catch (e: NoTransformationFoundException) {
            logMessage(LogLevel.ERROR, "no transformation? 😕\n${e.message}")
        } catch (e: Exception) {
            logMessage(LogLevel.ERROR, "generateJson exception:\n${e.message}")
        }
        return null
    }
}

data class GeminiApiResponse<T>(
    val status: HttpStatusCode?,
    val data: T?
)

data class GeminiApiRequest<T>(
    val method: String,
    val body: T,
    val model: String? = null
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiRequestText>,
)

@Serializable
data class GeminiRequestText(
    val text: String
)

