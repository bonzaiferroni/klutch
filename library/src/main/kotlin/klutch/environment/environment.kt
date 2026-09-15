package klutch.environment

import kabinet.utils.Environment
import java.io.File

fun readEnvFromPathOrNull(path: String = "../.env") = File(path)
    .takeIf { it.exists() }
    ?.let { Environment.fromText(it.readText()) }

fun readEnvFromPath(path: String = "../.env") = readEnvFromPathOrNull(path) ?: error("Unable to find .env at $path")

class SystemEnvironment(private val fallback: Environment?): Environment {
    override fun readOrNull(key: String) = System.getenv(key) ?: fallback?.readOrNull(key)

    companion object {
        fun fromSystem(fallback: Environment? = null): Environment = SystemEnvironment(fallback)
    }
}