package klutch.server

import io.ktor.server.routing.Routing
import org.koin.core.Koin

class ServerContext(val koin: Koin) {
    inline fun <reified T> get(): T = koin.get()
}

class ApiContext(
    val server: ServerContext,
    context: Routing,
): Routing by context {
}

fun Routing.routingContextOf(
    server: ServerContext,
    block: ApiContext.() -> Unit
) = ApiContext(server, this).block()

