package klutch.db.services

import kabinet.model.EditUserRequest
import klutch.db.tables.UserTable
import kotlinx.datetime.Clock
import klutch.db.DbService
import kabinet.model.UserRole
import kabinet.model.User
import klutch.db.model.User as DbUser
import kabinet.model.PrivateInfo
import kabinet.model.SignUpRequest
import kabinet.utils.randomUuidString
import kabinet.utils.toLocalDateTimeUtc
import kabinet.utils.validEmail
import kabinet.utils.validPassword
import kabinet.utils.validUsernameChars
import kabinet.utils.validUsernameLength
import klutch.db.tables.UserAspect
import klutch.server.generateUniqueSalt
import klutch.server.hashPassword
import klutch.server.toBase64
import klutch.utils.eqLowercase
import klutch.utils.toUUID
import klutch.utils.serverLog
import org.jetbrains.exposed.sql.*

class UserApiService : DbService() {

    private fun readByUsername(username: String): DbUser? =
        UserAspect.readFirst { UserTable.username.lowerCase() eq username.lowercase() }

    suspend fun readByUsernameOrEmail(usernameOrEmail: String): DbUser? = dbQuery {
        UserAspect.readFirst {
            (UserTable.username.lowerCase() eq usernameOrEmail.lowercase()) or
                    (UserTable.email.lowerCase() eq usernameOrEmail.lowercase())
        }
    }

    suspend fun readIdByUsername(username: String) = dbQuery {
        UserTable.select(UserTable.id)
            .where { UserTable.username.eq(username) }
            .first()[UserTable.id].value
    }

    suspend fun readUserDto(username: String): User {
        val user = readByUsernameOrEmail(username) ?: throw IllegalArgumentException("User not found")
        return User(
            id = user.id,
            username = user.username,
            roles = user.roles,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }

    suspend fun createUser(
        info: SignUpRequest,
        userRoles: List<String> = listOf(UserRole.USER.name),
    ) = dbQuery {
        serverLog.logInfo("Creating user: ${info.username}")
        validateUsername(info)
        validateEmail(info)
        validatePassword(info)

        val uniqueSalt = generateUniqueSalt()
        val passwordHashed = hashPassword(info.password, uniqueSalt)
        val now = Clock.System.now()

        UserTable.insertAndGetId {
            it[id] = randomUuidString().toUUID()
            it[name] = info.name
            it[username] = info.username
            it[hashedPassword] = passwordHashed
            it[salt] = uniqueSalt.toBase64()
            it[email] = info.email
            it[roles] = userRoles
            it[createdAt] = now.toLocalDateTimeUtc()
            it[updatedAt] = now.toLocalDateTimeUtc()
        }.value
    }

    private fun validateUsername(info: SignUpRequest) {
        if (!info.username.validUsernameLength) throw IllegalArgumentException("Username should be least 3 characters.")
        if (!info.username.validUsernameChars) throw IllegalArgumentException("Username has invalid characters.")
        val usernameTaken = UserAspect.any { UserTable.username.lowerCase() eq info.username.lowercase() }
        if (usernameTaken) throw IllegalArgumentException("Username already exists.")
    }

    private fun validateEmail(info: SignUpRequest) {
        val email = info.email ?: return // email is optional
        if (!info.email.validEmail) throw IllegalArgumentException("Invalid email.")
        val emailTaken = UserAspect.any { UserTable.email.lowerCase() eq email.lowercase() }
        if (emailTaken) throw IllegalArgumentException("Email already exists.")
    }

    private fun validatePassword(info: SignUpRequest) {
        if (!info.password.validPassword) throw IllegalArgumentException("Password is too weak.")
    }

    suspend fun getPrivateInfo(username: String) = dbQuery {
        UserTable.select(UserTable.name, UserTable.email)
            .where { UserTable.username.eq(username) }
            .firstOrNull()
            ?.let { PrivateInfo(it[UserTable.name], it[UserTable.email]) }
            ?: throw IllegalArgumentException("User not found")
    }

    suspend fun updateUser(username: String, info: EditUserRequest) = dbQuery {
        if (info.deleteUser) {
            serverLog.logInfo("UserService: Deleting user $username")
            UserTable.deleteWhere { UserTable.username.eqLowercase(username) }
        } else {
            serverLog.logInfo("UserService: Updating user $username")
            UserTable.update({ UserTable.username.eqLowercase(username) }) {
                it[name] = info.name
                if (info.deleteName) it[name] = null
                it[email] = info.email
                if (info.deleteEmail) it[email] = null
                it[avatarUrl] = info.avatarUrl
                it[updatedAt] = Clock.System.now().toLocalDateTimeUtc()
            }
        }
    }
}