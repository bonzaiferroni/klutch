package klutch.gemini

import io.ktor.utils.io.charsets.Charset
import kabinet.model.SpeechGenRequest
import klutch.utils.pcmToWav
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import kotlin.concurrent.thread

class KokoroClient {

    fun getMessage(text: String) = runPyBytes("../../kokoro/speak.py", text)

    fun generateSpeech(request: SpeechGenRequest): String {
        val filename = request.filename?.let { toFilename(it) } ?: "${toFilename(request.text)}-${provideTimestamp()}"
        val folder = "wav"
        val path = "$folder/$filename.wav"
        val file = File(path)
        if (file.exists()) return path
        // val data = runPyBytes("../../kokoro/speak.py", request.text)
        val data = runPyBytes("py/speak.py", request.text, request.voice?.apiName)
        // val bytes = pcmToWav(Base64.getDecoder().decode(data))
        file.writeBytes(data)
        return path
    }

    private fun runPyText(script: String, vararg args: String, python: String = "python3"): String {
        val cmd = listOf(python, script) + args
        val proc = ProcessBuilder(cmd).redirectErrorStream(true).start()
        val out = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        if (code != 0) error("Python exited $code:\n$out")
        return out.trim()
    }

    fun runPyBytes(script: String, vararg args: String?, python: String = "python3"): ByteArray {
        val cmd = listOf(python, script) + args
        val proc = ProcessBuilder(cmd).start()

        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()

        val tOut = thread(start = true, name = "py-stdout") { proc.inputStream.use { it.copyTo(out) } }
        val tErr = thread(start = true, name = "py-stderr") { proc.errorStream.use { it.copyTo(err) } }

        val code = proc.waitFor()
        tOut.join()
        tErr.join()

        if (code != 0) {
            val errText = err.toByteArray().toString(Charset.defaultCharset())
            error("Python exited $code:\n$errText")
        }
        return out.toByteArray()
    }
}