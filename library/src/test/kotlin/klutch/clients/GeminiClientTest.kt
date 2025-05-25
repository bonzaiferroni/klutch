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
 * We be testin' the generateEmbeddings and requestJson functions to make sure
 * they be returnin' proper treasure when the response be successful.
 */
class GeminiClientTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var mockClient: HttpClient
    private lateinit var geminiClient: GeminiClient
    private val testApiKey = "test_api_key"
    private val logMessages = mutableListOf<Triple<String, LogLevel, String>>()

    @BeforeTest
    fun setup() {
        // Create a mock engine that responds based on the URL
        mockEngine = MockEngine { request ->
            val url = request.url.toString()

            when {
                // For embeddings endpoint
                url.contains("text-embedding-004:embedContent") -> {
                    val mockEmbeddingResponse = """
                        {
                            "embedding": {
                                "values": [0.1, 0.2, 0.3, 0.4, 0.5]
                            }
                        }
                    """.trimIndent()

                    respond(
                        content = mockEmbeddingResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }

                // For generateContent endpoint
                url.contains("generateContent") -> {
                    val mockJsonResponse = """
                        {
                            "candidates": [
                                {
                                    "content": {
                                        "parts": [
                                            {
                                                "text": "{\"message\":\"Ahoy matey!\",\"value\":42}"
                                            }
                                        ]
                                    },
                                    "finishReason": "STOP"
                                }
                            ],
                            "usageMetadata": {
                                "promptTokenCount": 10,
                                "candidatesTokenCount": 5,
                                "totalTokenCount": 15,
                                "promptTokensDetails": [
                                    {
                                        "modality": "text",
                                        "tokenCount": 10
                                    }
                                ]
                            },
                            "modelVersion": "gemini-1.5-flash"
                        }
                    """.trimIndent()

                    respond(
                        content = mockJsonResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }

                // Default response for any other URL
                else -> {
                    respond(
                        content = "{}",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
        }

        mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
        }

        geminiClient = GeminiClient(
            limitedToken = testApiKey,
            client = mockClient,
            logMessage = { source, level, message ->
                logMessages.add(Triple(source, level, message))
            }
        )

        logMessages.clear()
    }

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

    @Test
    fun `test requestJson returns data on successful response`() {
        // Define a test data class
        @kotlinx.serialization.Serializable
        data class TestResponse(val message: String, val value: Int)

        // Execute the function
        runTest {
            val result = geminiClient.requestJson<TestResponse>(maxAttempts = 1, "Give me a test response")

            // Verify result
            assertNotNull(result, "JSON result should not be null")
            assertEquals("Ahoy matey!", result.message, "Message should match expected value")
            assertEquals(42, result.value, "Value should match expected value")
        }
    }
}
