package klutch.gemini

import io.ktor.server.routing.Routing
import kampfire.api.SpeechApi
import klutch.server.postEndpoint

fun Routing.serveSpeech(api: SpeechApi, service: SpeechService) {
    postEndpoint(api.url) {
        service.generateSpeechUrl(it.data)
    }

    postEndpoint(api.wav) {
        service.generateSpeech(it.data)
    }
}