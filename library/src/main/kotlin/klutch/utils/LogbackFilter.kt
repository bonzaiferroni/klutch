package klutch.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import kabinet.console.globalConsole

private val console = globalConsole.getHandle(LogbackFilter::class)

class LogbackFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        val throwable = event.throwableProxy ?: return FilterReply.NEUTRAL
        val className = throwable.className
        return if (deniedClassnames.contains(className)) {
            console.logWarning("$className ${throwable.message}")
            FilterReply.DENY
        }
        else
            FilterReply.NEUTRAL
    }
}

val deniedClassnames = setOf(
    "io.ktor.utils.io.ClosedByteChannelException",
    "java.nio.channels.ClosedChannelException",
    "java.net.SocketException"
)
