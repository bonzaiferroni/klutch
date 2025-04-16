package klutch.environment

import java.io.File

fun readEnvFromPathOrNull(path: String = "../.env") = File(path)
    .takeIf { it.exists() }
    ?.let { readEnvFromText(it.readText()) }

fun readEnvFromPath(path: String = "../.env") = readEnvFromPathOrNull(path) ?: error("Unable to find .env at $path")

fun readEnvFromText(text: String): Environment = MapEnvironment(text)

interface Environment {
    fun read(key: String): String
}

internal class MapEnvironment(content: String): Environment {
    private val map = mutableMapOf<String, String>()

    init {
        val lines = content.split('\n')
        for (line in lines) {
            val values = line.trim().split('=')
            if (values.size != 2) continue
            map[values[0]] = values[1]
        }
    }

    override fun read(key: String) = map.getValue(key)
}