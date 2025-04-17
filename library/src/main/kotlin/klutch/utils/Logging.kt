package klutch.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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