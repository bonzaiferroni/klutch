package klutch.gemini

import kabinet.clients.GeminiMessage
import kabinet.console.LogLevel
import kabinet.console.globalConsole
import kabinet.gemini.GeminiClient
import kabinet.gemini.generateSpeech
import kabinet.gemini.generateEmbedding
import kabinet.gemini.generateImage
import kabinet.gemini.generateTextFromMessages
import kabinet.gemini.generateTextFromParts
import kabinet.model.ImageGenRequest
import kabinet.model.ImageUrls
import kabinet.model.SpeechRequest
import kabinet.utils.Environment
import kabinet.utils.toBase62
import klutch.environment.readEnvFromPath
import klutch.utils.pcmToWav
import klutch.utils.writePngThumbnail
import kotlinx.datetime.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private val console = globalConsole.getHandle(GeminiService::class)

class GeminiService(
    private val env: Environment = readEnvFromPath(),
    val client: GeminiClient = GeminiClient(
        token = env.read("GEMINI_KEY_RATE_LIMIT_A"),
        backupToken = env.read("GEMINI_KEY_RATE_LIMIT_B"),
        logMessage = log::message,
    )
) {
    suspend inline fun <reified T> requestJson(vararg parts: String) = client.generateJson<T>(*parts)

    suspend fun generateEmbedding(text: String): FloatArray? {
        val filename = compressToFilenameSafe(text).take(200)
        val folder = "emb"
        val path = "$folder/$filename.vec"
        val file = File(path)
        if (file.exists()) file.readBytes().toFloatArray()
        val array = client.generateEmbedding(text)?.normalize() ?: return null
        file.writeBytes(array.toByteArray())
        return array
    }

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

    suspend fun generateSpeech(request: SpeechRequest): ByteArray? {
        val file = request.takeIf { it.isCached }?.let { File(pathOf(it)) }
        if (file != null && file.exists()) return file.readBytes().also { console.log("returning cached speech") }
        val data = client.generateSpeech(request.text, request.theme, request.voice?.apiName) ?: return null
        val bytes = pcmToWav(Base64.getDecoder().decode(data))
        file?.writeBytes(bytes)
        return bytes
    }

    suspend fun generateSpeechUrl(request: SpeechRequest): String? {
        val path = pathOf(request)
        val file = File(path)
        if (file.exists()) return path
        val data = client.generateSpeech(request.text, request.theme, request.voice?.apiName) ?: return null
        val bytes = pcmToWav(Base64.getDecoder().decode(data))
        file.writeBytes(bytes)
        return path
    }

    private fun pathOf(request: SpeechRequest): String {
        val filename = request.filename?.let { toFilename(it) }
            ?: "${toFilename(request.text)}-${request.voice?.apiName}"
        val folder = "wav"
        return "$folder/$filename.wav"
    }
}

private val log = LoggerFactory.getLogger("Gemini")

fun Logger.message(level: LogLevel, msg: String) = when(level) {
    LogLevel.Trace -> this.trace(msg)
    LogLevel.Debug -> this.debug(msg)
    LogLevel.Info  -> this.info(msg)
    LogLevel.Warning  -> this.warn(msg)
    LogLevel.Error -> this.error(msg)
}

fun toFilename(input: String): String =
    input
        .take(64).lowercase()
        .replace(Regex("[^A-Za-z0-9]"), "_")

fun provideTimestamp() = Clock.System.now().toEpochMilliseconds().toBase62()

fun FloatArray.normalize(): FloatArray {
    val norm = kotlin.math.sqrt(this.sumOf { (it * it).toDouble() }).toFloat()
    return if (norm == 0f) this else FloatArray(size) { this[it] / norm }
}

fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(size * 4).order(ByteOrder.BIG_ENDIAN)
    for (f in this) buffer.putFloat(f)
    return buffer.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
    val floats = FloatArray(size / 4)
    for (i in floats.indices) floats[i] = buffer.getFloat()
    return floats
}

fun compressToFilenameSafe(text: String): String {
    val raw = text.toByteArray(Charsets.UTF_8)
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { it.write(raw) }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray())
}

fun decompressFromFilenameSafe(namePart: String): String {
    val compressed = Base64.getUrlDecoder().decode(namePart)
    val gis = GZIPInputStream(ByteArrayInputStream(compressed))
    val out = ByteArrayOutputStream()
    gis.use { it.copyTo(out) }
    return out.toString(Charsets.UTF_8)
}