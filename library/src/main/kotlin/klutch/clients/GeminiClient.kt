package klutch.clients

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import klutch.log.LogLevel
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.seconds

class GeminiClient(
    val token: String,
    val backupToken: String? = null,
    val model: String = "gemini-2.5-flash",
    val client: HttpClient = ktorClient,
    val logMessage: (LogLevel, String) -> Unit
) {
    suspend inline fun <reified T> tryRequest(requestBlock: suspend () -> GeminiApiRequest<T>): HttpResponse? {
        var usedToken = token
        repeat(3) {
            try {
                val request = requestBlock()
                val model = request.model ?: model
                println(usedToken)
                val url = generateUrl(model, request.method, usedToken)
                val ktorRequest = HttpRequestBuilder().apply {
                    method = HttpMethod.Post
                    url(url)
                    contentType(ContentType.Application.Json)
                    setBody(request.body)
                }
                val response = client.request(ktorRequest)
                if (response.status == HttpStatusCode.TooManyRequests) {
                    usedToken = backupToken ?: return null
                    println("Too many request, trying backup token")
                    return@repeat
                }
                if (response.status == HttpStatusCode.InternalServerError) {
                    println("Internal server error, delaying")
                    delay(10.seconds)
                    return@repeat
                }
                if (response.status != HttpStatusCode.OK) {
                    logMessage(LogLevel.ERROR, "Error generating Json:\n${response.body<JsonObject>()}")
                }
                return response
            } catch (e: HttpRequestTimeoutException) {
                logMessage(LogLevel.ERROR, "Request timed out")
                return null
            } catch (e: NoTransformationFoundException) {
                logMessage(LogLevel.ERROR, "no transformation? 😕\n${e.message}")
                return null
            } catch (e: Exception) {
                logMessage(LogLevel.ERROR, "generateJson exception:\n${e.message}")
                return null
            }
        }
        return null
    }

    fun generateUrl(model: String, method: String, token: String) =
        "https://generativelanguage.googleapis.com/v1beta/models/$model:$method?key=$token"
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
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiBlob? = null,
)

@Serializable
data class GeminiBlob(
    val mimeType: String? = null,
    val data: String, //  (bytes format) Raw bytes for media formats. A base64-encoded string.
)

