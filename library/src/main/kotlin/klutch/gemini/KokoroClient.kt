package klutch.gemini

import io.ktor.utils.io.charsets.Charset
import jep.Interpreter
import jep.SharedInterpreter
import kabinet.model.SpeechRequest
import kabinet.model.SpeechVoice
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.concurrent.thread
import kotlin.use

class KokoroClient {

    fun getMessageA(text: String) = runPyBytes("py/speak.py", text)

    fun getMessageB(text: String) = runPyJep(text)

    fun generateCacheSpeech(request: SpeechRequest): String {
        val filename = request.filename?.let { toFilename(it) } ?: "${toFilename(request.text)}-${provideTimestamp()}"
        val folder = "wav"
        val path = "$folder/$filename.wav"
        val file = File(path)
        if (file.exists()) return path
        // val data = runPyBytes("../../kokoro/speak.py", request.text)
        val data = runPyBytes("py/speak.py", request.text, request.voice?.apiName ?: SpeechVoice.Sky.apiName)
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

    fun runPyJep(text: String): ByteArray {
        SharedInterpreter().use { interp: Interpreter ->
            // Import the script
            val pyDir = File("py").absoluteFile.path   // folder that holds speak.py and the other script
            interp.set("PY_DIR", pyDir)
            interp.exec("import sys; sys.path.insert(0, PY_DIR)")  // make Python see yer modules
            interp.exec("import speak") // just import, __name__ = 'speak', so guard won’t run

            // Call the function
            return interp.invoke("speak.tts_bytes", text) as ByteArray
        }
    }
}