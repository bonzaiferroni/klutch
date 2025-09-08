package klutch.utils

import io.ktor.utils.io.charsets.Charset
import kotlinx.io.files.Path
import java.io.ByteArrayOutputStream
import java.io.File

fun runExeBytes(executablePath: String, vararg args: String): ByteArray {
    val exe = File(executablePath)
    require(exe.exists()) { "Missing executable: $executablePath" }
    if (!exe.canExecute()) exe.setExecutable(true)

    val cmd = listOf(exe.absolutePath) + args
    val proc = ProcessBuilder(cmd)
        .redirectErrorStream(true)
        .start()

    val out = ByteArrayOutputStream()
    proc.inputStream.use { it.copyTo(out) }

    val code = proc.waitFor()
    if (code != 0) error("Tool exited $code:\n${out.toString(Charset.defaultCharset())}")
    return out.toByteArray()
}

fun resolveTool(baseName: String, dir: String): String {
    val isWin = System.getProperty("os.name").lowercase().contains("win")
    val name = if (isWin) "$baseName.exe" else baseName
    return File(dir, name).absolutePath
}