package klutch.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

val dbLog = Log(LoggerFactory.getLogger("db"))
val serverLog = Log(LoggerFactory.getLogger("server"))

class Log(
    private val logger: Logger
) {
    fun logInfo(message: String) {
        logger.info(message)
    }

    fun logError(message: String) {
        logger.error(message)
    }

    fun logWarn(message: String) {
        logger.warn(message)
    }

    fun logDebug(message: String) {
        logger.debug(message)
    }

    fun logTrace(message: String) {
        logger.trace(message)
    }
}

fun KotlinLogging.logger(type: KClass<*>) = logger(type::simpleName.name)
fun KotlinLogging.logger(function: KFunction<*>) = logger(function.name)