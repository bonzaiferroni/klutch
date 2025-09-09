package klutch.gemini

import io.ktor.http.HttpStatusCode
import kabinet.gemini.GeminiApiResponse
import kabinet.gemini.GeminiClient
import klutch.log.LogLevel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class GeminiClientPool(
    val limitedClient: GeminiClient,
    val unlimitedClient: GeminiClient? = null,
    val logMessage: (LogLevel, String) -> Unit
) {
    var unlimitedUntil: Instant = Instant.DISTANT_PAST
    var restingUntil: Instant = Instant.DISTANT_PAST

    suspend fun <T> tryRequest(
        client: GeminiClient = limitedClient,
        requestBlock: suspend (GeminiClient) -> GeminiApiResponse<T>?
    ): GeminiApiResponse<T>? {
        while (restingUntil > Clock.System.now()) {
            delay((restingUntil - Clock.System.now()))
        }
        restingUntil = Clock.System.now() + 6.seconds

        return requestBlock(client)
    }

    suspend fun <T> tryToken(requestBlock: suspend (GeminiClient) -> GeminiApiResponse<T>?): GeminiApiResponse<T>? {
        val isUnlimited = unlimitedClient != null && Clock.System.now() < unlimitedUntil
        if (isUnlimited) logMessage(LogLevel.INFO, "Unlimited Token Used")

        val client = when {
            isUnlimited -> unlimitedClient
            else -> limitedClient
        }

        val response = tryRequest(client, requestBlock)
        if (response?.status == HttpStatusCode.TooManyRequests) {
            if (isUnlimited) {
                restingUntil = Clock.System.now() + 1.hours
                logMessage(LogLevel.ERROR, "Rate limit reached on unlimited token, resting")
            } else {
                unlimitedUntil = Clock.System.now() + 4.hours
                logMessage(LogLevel.ERROR, "Token limit reached, attempting unlimited request")
                return tryToken(requestBlock)
            }
        }
        return response
    }
}