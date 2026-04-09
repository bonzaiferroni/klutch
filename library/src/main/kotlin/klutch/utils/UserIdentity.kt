package klutch.utils

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import kampfire.model.UserId
import klutch.db.services.BasicUserTableDao
import klutch.server.CLAIM_ROLES
import klutch.server.CLAIM_USERNAME

data class UserIdentity(val userId: UserId, val username: String)

suspend fun RoutingContext.getUserIdentityOrNull(): UserIdentity? {
    val username = getClaimOrNull(CLAIM_USERNAME) ?: return null
    val userId = BasicUserTableDao().readIdByUsername(username)?.toStringId()?.let { UserId(it) } ?: return null
    return UserIdentity(userId, username)
}

suspend fun RoutingContext.getUserIdentity() = getUserIdentityOrNull() ?: error("user identity not found")

suspend fun RoutingContext.getUserIdOrNull() = getUserIdentityOrNull()?.userId

suspend fun RoutingContext.getUserId() = getUserIdOrNull() ?: error("user id not provided")

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