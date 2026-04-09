package klutch.db.services

import kabinet.console.globalConsole
import kampfire.model.SignUpRequest
import kampfire.model.UserRole
import kabinet.utils.Environment
import kampfire.model.AuthUser
import kampfire.model.UserSeed

private val console = globalConsole.getHandle(UserInitService::class)

class UserInitService<T: AuthUser>(
    private val env: Environment,
    private val dao: AuthDao<T>,
    private val provideUser: (UserSeed) -> T,
) {
    suspend fun initUsers() {
        val service = AuthService(dao, provideUser)
        val username = env.read(ADMIN_USERNAME_KEY)
        val existingUser = dao.readByUsernameOrEmail(username)
        if (existingUser != null) return
        console.log("Initializing admin user: $username")
        val email = env.read(ADMIN_EMAIL_KEY)
        val password = env.read(ADMIN_PASSWORD_KEY)
        service.createUser(
            info = SignUpRequest(
                username = username,
                password = password,
                email = email,
            ),
            roles = setOf(UserRole.USER, UserRole.ADMIN)
        )
    }
}

const val ADMIN_EMAIL_KEY = "ADMIN_EMAIL"
const val ADMIN_PASSWORD_KEY = "ADMIN_PASSWORD"
const val ADMIN_USERNAME_KEY = "ADMIN_USERNAME"