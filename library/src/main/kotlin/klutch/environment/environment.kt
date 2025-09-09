package klutch.environment

import kabinet.utils.readEnvFromText
import java.io.File

fun readEnvFromPathOrNull(path: String = "../.env") = File(path)
    .takeIf { it.exists() }
    ?.let { readEnvFromText(it.readText()) }

fun readEnvFromPath(path: String = "../.env") = readEnvFromPathOrNull(path) ?: error("Unable to find .env at $path")