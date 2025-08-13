package klutch.gemini

import kabinet.clients.GeminiMessage
import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechGenRequest
import kabinet.utils.toBase62
import klutch.environment.Environment
import klutch.environment.readEnvFromPath
import klutch.log.LogLevel
import klutch.utils.pcmToWav
import klutch.utils.writePngThumbnail
import kotlinx.datetime.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Base64

class GeminiService(
    private val env: Environment = readEnvFromPath(),
    val client: GeminiClient = GeminiClient(
        token = env.read("GEMINI_KEY_RATE_LIMIT_A"),
        backupToken = env.read("GEMINI_KEY_RATE_LIMIT_B"),
        logMessage = log::message,
    )
) {
    suspend inline fun <reified T> requestJson(vararg parts: String) = client.generateJson<T>(*parts)

    suspend fun generateEmbeddings(text: String): FloatArray? = client.generateEmbeddings(text)

    suspend fun generateText(vararg parts: String) = client.generateTextFromParts(*parts)

    suspend fun chat(messages: List<GeminiMessage>) = client.generateTextFromMessages(messages)

    suspend fun generateImage(request: ImageGenRequest): ImageUrls {
        val filename = request.filename?.let { toFilename(it) } ?: "${toFilename(request.text)}-${provideTimestamp()}"
        val folder = "img"
        val path = "$folder/$filename.png"
        val thumbPath = "$folder/$filename-thumb.png"
        val file = File(path)
        if (file.exists()) return ImageUrls(
            url = path,
            thumbUrl = thumbPath
        )
        val data = client.generateImage(request.text, request.theme) ?: error("Unable to generate image")
        val bytes = Base64.getDecoder().decode(data)
        file.writeBytes(bytes)
        writePngThumbnail(bytes, thumbPath)
        return ImageUrls(path, thumbPath)
    }

    suspend fun generateSpeech(request: SpeechGenRequest): String {
        val filename = request.filename?.let { toFilename(it) } ?: "${toFilename(request.text)}-${provideTimestamp()}"
        val folder = "wav"
        val path = "$folder/$filename.wav"
        val file = File(path)
        if (file.exists()) return path
        val data = client.generateSpeech(request.text, request.theme, request.voice?.apiName)
            ?: error("Unable to generate speech")
        val bytes = pcmToWav(Base64.getDecoder().decode(data))
        file.writeBytes(bytes)
        return path
    }
}

private val log = LoggerFactory.getLogger("Gemini")

fun Logger.message(level: LogLevel, msg: String) = when(level) {
    LogLevel.TRACE -> this.trace(msg)
    LogLevel.DEBUG -> this.debug(msg)
    LogLevel.INFO  -> this.info(msg)
    LogLevel.WARNING  -> this.warn(msg)
    LogLevel.ERROR -> this.error(msg)
}

fun toFilename(input: String): String =
    input
        .take(64).lowercase()
        .replace(Regex("[^A-Za-z0-9]"), "_")

fun provideTimestamp() = Clock.System.now().toEpochMilliseconds().toBase62()
