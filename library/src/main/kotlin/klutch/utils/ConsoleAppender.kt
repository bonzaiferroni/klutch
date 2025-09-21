package klutch.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import kabinet.console.LogLevel
import kabinet.console.globalConsole
import ch.qos.logback.classic.Level

class ConsoleAppender(
    private val handler: (ILoggingEvent) -> Unit = ::defaultHandler
) : AppenderBase<ILoggingEvent>() {

    override fun append(eventObject: ILoggingEvent) {
        try {
            handler(eventObject)      // yer custom handling here (JSON, ANSI, ship it to WS, etc.)
        } catch (_: Throwable) {
            // swallow so logging never sinks the ship
        }
    }

    companion object {
        private fun defaultHandler(e: ILoggingEvent) {
            // example: print yer own format
            // val line = "${e.level} ${e.loggerName} - ${e.formattedMessage}"
            // println(line)
            if (e.level == Level.OFF) return
            val throwable = e.throwableProxy  // just the exception message

            val msg = buildString {
                append("${e.level} ${e.loggerName} - ${e.formattedMessage}")
                if (throwable != null) {
                    appendLine()
                    append("${throwable.className}: ${throwable.message}")
                }
            }

            globalConsole.log(e.loggerName, e.level.toLogLevel(), msg)
        }
    }
}

fun Level.toLogLevel(): LogLevel = when(this) {
    Level.ERROR -> LogLevel.Error
    Level.WARN -> LogLevel.Warning
    Level.INFO -> LogLevel.Info
    Level.DEBUG -> LogLevel.Debug
    Level.TRACE -> LogLevel.Trace
    else -> LogLevel.Info
}