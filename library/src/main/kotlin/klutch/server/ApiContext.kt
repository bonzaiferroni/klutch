package klutch.server

import io.ktor.server.routing.Routing
import kampfire.model.AuthUser
import klutch.db.services.AuthId
import klutch.utils.Identity

class ApiContext<out Model>(
    val model: Model,
    context: Routing,
): Routing by context {
}

fun <T> Routing.routingContextOf(
    model: T,
    block: ApiContext<T>.() -> Unit
) = ApiContext(model, this).block()

