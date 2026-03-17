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

suspend fun RoutingContext.getUserId() = getUserIdOrNull() ?: error("user id not provided")

suspend fun RoutingContext.getUserIdOrNull() = getClaimOrNull(CLAIM_USERNAME)?.let { username ->
    UserTableService().readIdByUsername(username)?.toStringId()?.let { UserId(it) }
}

fun RoutingContext.getUsernameOrNull(): String? {
    return getClaimOrNull(CLAIM_USERNAME)
}

fun RoutingContext.getUsername(): String {
    return getClaimOrNull(CLAIM_USERNAME) ?: error("username not provided")
}

fun RoutingContext.testRole(role: String): Boolean {
    return getClaimOrNull(CLAIM_ROLES)?.contains(role) ?: false
}

fun RoutingContext.getClaimOrNull(name: String): String? {
    return call.principal<JWTPrincipal>()?.payload?.getClaim(name)?.asString()
}

suspend inline fun <reified T: Any> RoutingContext.okData(data: T) {
    call.respond(HttpStatusCode.OK, data)
}