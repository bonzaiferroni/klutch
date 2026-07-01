package klutch.db.services

import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.model.SignUpRequest
import kampfire.model.UserRole
import kabinet.utils.Environment
import kampfire.api.toUsername
import klutch.server.Authorizer
import klutch.server.ProviderScope
import klutch.server.provide

private val console = KotlinLogging.logger(::initUsers.name)

suspend fun initUsers(provider: ProviderScope) {
    val authorizer = provider.provide<Authorizer>()
    val env = provider.provide<Environment>()
    val session = provider.provide<SessionService>()
    val username = env.read(ADMIN_USERNAME_KEY).toUsername()
    val id = session.readIdByUsername(username)
    if (id != null) return
    console.info { "Initializing admin user: $username" }
    val email = env.read(ADMIN_EMAIL_KEY)
    val password = env.read(ADMIN_PASSWORD_KEY)
    authorizer.createUser(
        request = SignUpRequest(
            username = username,
            password = password,
            email = email,
        ),
        roles = setOf(UserRole.User, UserRole.Admin)
    )
}

const val ADMIN_EMAIL_KEY = "ADMIN_EMAIL"
const val ADMIN_PASSWORD_KEY = "ADMIN_PASSWORD"
const val ADMIN_USERNAME_KEY = "ADMIN_USERNAME"