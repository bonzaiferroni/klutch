package klutch.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kampfire.model.UserId
import klutch.db.services.UserTableService
import klutch.server.CLAIM_ROLES
import klutch.server.CLAIM_USERNAME

suspend fun RoutingContext.getUserId() = getClaim(CLAIM_USERNAME).let {
    UserTableService().readIdByUsername(it).toStringId().let { UserId(it) }
}

fun RoutingContext.getUsername(): String {
    return getClaim(CLAIM_USERNAME)
}

fun RoutingContext.testRole(role: String): Boolean {
    return getClaim(CLAIM_ROLES).contains(role)
}

fun RoutingContext.getClaim(name: String): String {
    return call.principal<JWTPrincipal>()?.payload?.getClaim(name)?.asString() ?: ""
}

suspend inline fun <reified T: Any> RoutingContext.okData(data: T) {
    call.respond(HttpStatusCode.OK, data)
}