package klutch.db.services

import kampfire.model.UserRole
import kampfire.model.SignUpRequest
import kabinet.utils.validEmail
import kabinet.utils.validPassword
import kabinet.utils.validUsernameChars
import kabinet.utils.validUsernameLength
import kampfire.model.AuthUser
import kampfire.model.UserSeed
import klutch.db.tables.BasicUserTable
import klutch.server.generateSalt
import klutch.server.hashPassword
import klutch.server.toBase64
import klutch.utils.serverLog
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class AuthService<T: AuthUser>(
    private val dao: AuthDao<T>,
    private val provideUser: (UserSeed) -> T
) {
    suspend fun createUser(
        info: SignUpRequest,
        roles: Set<UserRole> = setOf(UserRole.USER),
    ) {
        serverLog.logInfo("Creating user: ${info.username}")
        validateUsername(info)
        validateEmail(info)
        validatePassword(info)

        val salt = generateUniqueSalt()
        val hashedPassword = hashPassword(info.password, salt)
        val seed = UserSeed(
            request = info,
            salt = salt.toBase64(),
            hashedPassword = hashedPassword,
            roles = roles
        )

        val user = provideUser(seed)

        dao.createUser(user)
    }

    private suspend fun validateUsername(info: SignUpRequest) {
        if (!info.username.validUsernameLength) throw IllegalArgumentException("Username should be least 3 characters.")
        if (!info.username.validUsernameChars) throw IllegalArgumentException("Username has invalid characters.")
        val user = dao.readByUsernameOrEmail(info.username)
        if (user != null) throw IllegalArgumentException("Username already exists.")
    }

    private suspend fun validateEmail(info: SignUpRequest) {
        val email = info.email ?: return // email is optional
        if (!info.email.validEmail) throw IllegalArgumentException("Invalid email.")
        val user = dao.readByUsernameOrEmail(email)
        if (user != null) throw IllegalArgumentException("Email already exists.")
    }

    private fun validatePassword(info: SignUpRequest) {
        if (!info.password.validPassword) throw IllegalArgumentException("Password is too weak.")
    }

    private suspend fun generateUniqueSalt(): ByteArray {
        while (true) {
            val salt = generateSalt()
            val saltExists = dao.readSaltExists(salt.toBase64())
            if (!saltExists) return salt
        }
    }
}