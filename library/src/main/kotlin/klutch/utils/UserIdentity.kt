package klutch.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import kampfire.model.AuthUser
import kampfire.model.UserRole
import kampfire.model.toUserRoleSet
import klutch.db.services.AuthDao
import klutch.db.services.AuthId
import klutch.server.CLAIM_ROLES
import klutch.server.CLAIM_USERNAME

data class UserIdentity<Id: AuthId>(val userId: Id, val username: String, val roles: Set<UserRole>) {
    val isAdmin get() = roles.contains(UserRole.Admin)
}

class Identity<User: AuthUser, Id: AuthId>(
    private val dao: AuthDao<User, Id>
) {
    suspend fun getIdentityOrNull(call: ApplicationCall): UserIdentity<Id>? {
        val username = getClaimOrNull(call, CLAIM_USERNAME) ?: return null
        val roles = getClaimOrNull(call, CLAIM_ROLES)?.toUserRoleSet() ?: return null
        val userId = dao.readIdByUsername(username) ?: return null
        return UserIdentity(userId, username, roles)
    }

    suspend fun getIdentity(call: ApplicationCall) = getIdentityOrNull(call) ?: error("user identity not found")

    suspend fun getUserIdOrNull(call: ApplicationCall) = getIdentityOrNull(call)?.userId

    suspend fun getUserId(call: ApplicationCall) = getUserIdOrNull(call) ?: error("user id not provided")

    fun getUsernameOrNull(call: ApplicationCall): String? {
        return getClaimOrNull(call, CLAIM_USERNAME)
    }

    fun getUsername(call: ApplicationCall): String {
        return getClaimOrNull(call, CLAIM_USERNAME) ?: error("username not provided")
    }

    fun testRole(call: ApplicationCall, role: String): Boolean {
        return getClaimOrNull(call, CLAIM_ROLES)?.contains(role) ?: false
    }

    fun getClaimOrNull(call: ApplicationCall, name: String): String? {
        return call.principal<JWTPrincipal>()?.payload?.getClaim(name)?.asString()
    }
}