package klutch.server

import io.ktor.server.routing.Routing
import kampfire.model.AuthUser
import klutch.db.services.AuthId
import klutch.utils.Identity

class ApiContext<out Model, User: AuthUser, Id: AuthId>(
    val model: Model,
    context: Routing,
    val identity: Identity<User, Id>
): Routing by context {
}

fun <T, User: AuthUser, Id: AuthId> Routing.routingContextOf(
    model: T,
    identity: Identity<User, Id>,
    block: ApiContext<T, User, Id>.() -> Unit
) = ApiContext(model, this, identity).block()

