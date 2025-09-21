package klutch.db.services

import kabinet.console.globalConsole
import kabinet.model.SignUpRequest
import kabinet.model.UserRole
import kabinet.utils.Environment
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.count
import klutch.db.model.User
import klutch.db.read
import klutch.db.tables.UserTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insertIgnore
import java.io.File

private val console = globalConsole.getHandle(UserInitService::class)

class UserInitService(
    private val env: Environment,
    private val service: UserApiService = UserApiService()
) : DbService() {
    suspend fun initUsers() = dbQuery {
        val username = env.read(ADMIN_USERNAME_KEY)
        if (UserTable.count { it.username.eq(username) } > 0) return@dbQuery
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
            userRoles = listOf(UserRole.USER.name, UserRole.ADMIN.name)
        )
    }
}

const val ADMIN_EMAIL_KEY = "ADMIN_EMAIL"
const val ADMIN_NAME_KEY = "ADMIN_NAME"
const val ADMIN_PASSWORD_KEY = "ADMIN_PASSWORD"
const val ADMIN_USERNAME_KEY = "ADMIN_USERNAME"