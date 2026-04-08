package klutch.db.services

import kabinet.console.globalConsole
import kampfire.model.SignUpRequest
import kampfire.model.UserRole
import kabinet.utils.Environment
import kampfire.model.BasicUser
import kampfire.model.provideBasicUser
import klutch.db.DbService
import klutch.db.count
import klutch.db.tables.BasicUserTable
import org.jetbrains.exposed.v1.core.eq

private val console = globalConsole.getHandle(UserInitService::class)

class UserInitService(
    private val env: Environment,
    private val service: CreateUserService<BasicUser> = CreateUserService(BasicUserTableDao(), ::provideBasicUser)
) : DbService() {
    suspend fun initUsers() = dbQuery {
        val username = env.read(ADMIN_USERNAME_KEY)
        if (BasicUserTable.count { it.username.eq(username) } > 0) return@dbQuery
        console.log("Initializing admin user: $username")
        val name = env.read(ADMIN_NAME_KEY)
        val email = env.read(ADMIN_EMAIL_KEY)
        val password = env.read(ADMIN_PASSWORD_KEY)
        service.createUser(
            info = SignUpRequest(
                username = username,
                password = password,
                email = email,
                name = name
            ),
            roles = setOf(UserRole.USER, UserRole.ADMIN)
        )
    }
}

const val ADMIN_EMAIL_KEY = "ADMIN_EMAIL"
const val ADMIN_NAME_KEY = "ADMIN_NAME"
const val ADMIN_PASSWORD_KEY = "ADMIN_PASSWORD"
const val ADMIN_USERNAME_KEY = "ADMIN_USERNAME"