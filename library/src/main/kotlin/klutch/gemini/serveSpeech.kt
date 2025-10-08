package klutch.gemini

import io.ktor.server.routing.Routing
import kabinet.api.SpeechApi
import klutch.server.postEndpoint

fun Routing.serveSpeech(api: SpeechApi, service: SpeechService) {
    postEndpoint(api.url) { request, _ ->
        service.generateSpeechUrl(request)
    }

    postEndpoint(api.wav) { request, _ ->
        service.generateSpeech(request)
    }
}