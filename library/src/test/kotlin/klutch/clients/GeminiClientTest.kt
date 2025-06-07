package klutch.clients

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import klutch.log.LogLevel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * Arr, this be a test class for the GeminiClient extension functions.
 * We be testin' the generateEmbeddings and generateJson functions to make sure
 * they be returnin' proper treasure when the response be successful.
 */
class GeminiClientTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var mockClient: HttpClient
    private lateinit var geminiClient: GeminiClient
    private val testApiKey = "test_api_key"
    private val logMessages = mutableListOf<Triple<String, LogLevel, String>>()

    @Test
    fun `test generateEmbeddings returns data on successful response`() {
        // Execute the function
        runTest {
            val result = geminiClient.generateEmbeddings("Test text")

            // Verify result
            assertNotNull(result, "Embeddings result should not be null")
            assertEquals(5, result.size, "Embeddings should have 5 values")
            assertEquals(0.1f, result[0], "First embedding value should be 0.1")
        }
    }
}
