package klutch.gemini

import io.ktor.server.routing.Routing
import kabinet.gemini.GeminiApi
import klutch.server.*

/**
 * Arr! This be the route for the Gemini AI chat, ye scallywags!
 * It takes a list of messages and returns the AI's response.
 */
fun Routing.serveGemini(api: GeminiApi, service: GeminiService = GeminiService()) {
    // No need for authentication for this endpoint
    postEndpoint(api.chat) {
        // Send the messages to the AI and getEndpoint a response
        service.chat(it.data)
    }

    postEndpoint(api.image) {
        service.generateImage(it.data)
    }

    postEndpoint(api.speechUrl) {
        service.generateSpeechUrl(it.data)
    }

    postEndpoint(api.speech) {
        service.generateSpeech(it.data)
    }
}