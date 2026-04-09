package klutch.utils

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import kampfire.model.AuthUser
import klutch.db.services.AuthDao
import klutch.db.services.AuthId
import klutch.server.CLAIM_ROLES
import klutch.server.CLAIM_USERNAME

data class UserIdentity<Id: AuthId>(val userId: Id, val username: String)

class Identity<User: AuthUser, Id: AuthId>(
    private val dao: AuthDao<User, Id>
) {
    suspend fun getUserIdentityOrNull(call: RoutingCall): UserIdentity<Id>? {
        val username = getClaimOrNull(call, CLAIM_USERNAME) ?: return null
        val userId = dao.readIdByUsername(username) ?: return null
        return UserIdentity(userId, username)
    }

    suspend fun getUserIdentity(call: RoutingCall) = getUserIdentityOrNull(call) ?: error("user identity not found")

    suspend fun getUserIdOrNull(call: RoutingCall) = getUserIdentityOrNull(call)?.userId

    suspend fun getUserId(call: RoutingCall) = getUserIdOrNull(call) ?: error("user id not provided")

    fun getUsernameOrNull(call: RoutingCall): String? {
        return getClaimOrNull(call, CLAIM_USERNAME)
    }

    fun getUsername(call: RoutingCall): String {
        return getClaimOrNull(call, CLAIM_USERNAME) ?: error("username not provided")
    }

    fun testRole(call: RoutingCall, role: String): Boolean {
        return getClaimOrNull(call, CLAIM_ROLES)?.contains(role) ?: false
    }

    fun getClaimOrNull(call: RoutingCall, name: String): String? {
        return call.principal<JWTPrincipal>()?.payload?.getClaim(name)?.asString()
    }
}