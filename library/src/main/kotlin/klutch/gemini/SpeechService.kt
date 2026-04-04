package klutch.gemini

import kabinet.console.globalConsole
import kampfire.model.SpeechRequest
import java.io.File
import kotlin.io.readBytes

private val console = globalConsole.getHandle(SpeechService::class)

class SpeechService(
    private val provideSpeech: suspend (SpeechRequest) -> ByteArray?
) {
    suspend fun generateSpeech(request: SpeechRequest): ByteArray? {
        val file = request.takeIf { it.isCached }?.let { File(pathOf(it)) }
        if (file != null && file.exists()) return file.readBytes().also { console.log("returning cached speech") }
        console.log("generating speech: ${request.text.take(25)}")
        val bytes = provideSpeech(request) ?: return null
        file?.writeBytes(bytes)?.also { console.log("caching speech for future response") }
        return bytes
    }

    suspend fun generateSpeechUrl(request: SpeechRequest): String? {
        val path = pathOf(request)
        val file = File(path)
        if (file.exists()) return path.also { console.log("returning cached speech") }
        val bytes = provideSpeech(request) ?: return null
        file.writeBytes(bytes).also { console.log("caching speech for future response") }
        return path
    }

    private fun pathOf(request: SpeechRequest): String {
        val filename = request.toFilename()
        val folder = "wav"
        return "$folder/$filename.wav"
    }
}