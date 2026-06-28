package klutch.db.services

import io.github.oshai.kotlinlogging.KotlinLogging
import kabinet.console.globalConsole
import kampfire.model.SignUpRequest
import kampfire.model.UserRole
import kabinet.utils.Environment
import kampfire.api.toUsername
import kampfire.model.AuthUser

private val console = KotlinLogging.logger(UserInitService::class.simpleName!!)

class UserInitService<User: AuthUser, Id: AuthId>(
    private val env: Environment,
    private val dao: AuthDao<User, Id>,
) {
    suspend fun initUsers() {
        val service = AuthService(dao)
        val username = env.read(ADMIN_USERNAME_KEY).toUsername()
        val id = dao.readIdByUsername(username)
        if (id != null) return
        console.info { "Initializing admin user: $username" }
        val email = env.read(ADMIN_EMAIL_KEY)
        val password = env.read(ADMIN_PASSWORD_KEY)
        service.createUser(
            request = SignUpRequest(
                username = username,
                password = password,
                email = email,
            ),
            roles = setOf(UserRole.User, UserRole.Admin)
        )
    }
}

const val ADMIN_EMAIL_KEY = "ADMIN_EMAIL"
const val ADMIN_PASSWORD_KEY = "ADMIN_PASSWORD"
const val ADMIN_USERNAME_KEY = "ADMIN_USERNAME"