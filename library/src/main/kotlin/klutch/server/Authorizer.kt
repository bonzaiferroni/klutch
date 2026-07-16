package klutch.server

import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.api.Email
import kampfire.api.HashedPassword
import kampfire.api.Password
import kampfire.api.TableId
import kampfire.api.Username
import kampfire.api.toLoginIdentity
import kampfire.api.toValidOutcome
import kampfire.model.AccountType
import kampfire.model.HashedToken
import kampfire.model.LoginRequest
import kampfire.model.Ok
import kampfire.model.Problem
import kampfire.model.Outcome
import kampfire.model.Session
import kampfire.model.SignUpRequest
import kampfire.model.Token
import kampfire.model.UserRecord
import kampfire.model.UserRole
import kampfire.model.UserSeed
import kampfire.utils.deobfuscate
import klutch.db.services.AuthId
import klutch.db.services.SessionService
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger("authorize")

class Authorizer(
    val service: SessionService
) {
    suspend fun authorize(
        loginRequest: LoginRequest,
        guestToken: Token?,
    ): Outcome<Session> {
        val claimedUser = service.readByUsernameOrEmail(loginRequest.loginIdentity.toLoginIdentity())
            ?: return Problem("Invalid username")

        return when (val password = loginRequest.password?.deobfuscate()) {
            null -> when (guestToken) {
                null -> Problem("Missing password and token")
                else -> authorizeGuest(claimedUser, guestToken, loginRequest.isTemp)
            }
            else -> authorizePassword(claimedUser, password, loginRequest.isTemp)
        }
    }

    suspend fun authorizeNewAccount(userId: AuthId, stayLoggedIn: Boolean): Session {
        return createSession(userId, stayLoggedIn)
    }

    suspend fun authorizeGuest(
        claimedUser: UserRecord,
        token: Token,
        stayLoggedIn: Boolean,
    ): Outcome<Session> {
        val userToken = claimedUser.guestToken
        val invalidAccount = claimedUser.accountType != AccountType.Guest
                || claimedUser.activeAt < Clock.System.now() - GUEST_ACTIVITY_PERIOD
                || userToken == null
        if (invalidAccount) return Problem("Invalid account")

        val hashedToken = hashToken(token)
        if (hashedToken != userToken) return Problem("Invalid account")

        return Ok(createSession(claimedUser.userId, stayLoggedIn)).also {
            log.debug { "authorize: guest login"}
        }
    }

    suspend fun authorizePassword(
        claimedUser: UserRecord,
        givenPassword: Password,
        stayLoggedIn: Boolean,
    ): Outcome<Session> {
        val userPassword = claimedUser.hashedPassword ?: return Problem("Invalid password").also {
            log.info { "User has no password" }
        }

        if (!verifyPassword(givenPassword, userPassword)) {
            log.info { "Invalid password attempt" }
            return Problem("Invalid password")
        }

        return Ok(createSession(claimedUser.userId, stayLoggedIn)).also {
            log.debug { "authorize: password login" }
        }
    }

    suspend fun checkGuest(token: Token): Outcome<Username?> {
        return Ok(service.checkGuest(token))
    }

    // suspend fun createUser(request: SignUpRequest): Outcome<TableId<Uuid>> {
    //     return when (request.accountType) {
    //         AccountType.Guest -> createGuestUser(request)
    //         AccountType.Registered -> createRegisteredUser(request)
    //     }
    // }

    suspend fun createGuestUser(request: SignUpRequest, token: Token): Outcome<TableId<Uuid>> {
        val problem = getUsernameProblem(request.username)
        if (problem != null) return problem.also {
            log.info { "Create user problem: ${it.message}" }
        }

        val hashedToken = hashToken(token)
        val seed = UserSeed(
            request = request,
            hashedPassword = null,
            roles = setOf(UserRole.User),
            accountType = AccountType.Guest,
            guestToken = hashedToken,
        )

        return Ok(service.createUserRecord(seed))
    }

    suspend fun createRegisteredUser(
        request: SignUpRequest,
        roles: Set<UserRole>
    ): Outcome<TableId<Uuid>> {
        log.info { "Creating user" }
        val password = requireNotNull(request.password) { "Password was null" }

        val problem = getUsernameProblem(request.username) ?: getEmailProblem(request.email) ?: getPasswordProblem(password)
        if (problem != null) return problem.also {
            log.info { "Create user problem: ${it.message}" }
        }

        val hashedPassword = hashPassword(password)
        val seed = UserSeed(
            request = request,
            hashedPassword = hashedPassword,
            roles = roles,
            accountType = AccountType.Registered,
            guestToken = null,
        )

        return Ok(service.createUserRecord(seed))
    }

    private fun verifyPassword(password: Password, stored: HashedPassword): Boolean =
        BCrypt.verifyer().verify(password.value.toCharArray(), stored.value.toCharArray()).verified

    private suspend fun createSession(
        userId: TableId<Uuid>,
        isTemp: Boolean,
    ): Session {
        val token = generateToken()
        val hash = hashToken(token)
        val ttl = when(isTemp) {
            true -> TOKEN_TEMP_TTL
            false -> TOKEN_DEFAULT_TTL
        }
        val now = Clock.System.now()
        val expiresAt = Clock.System.now() + ttl
        service.createSessionRecord(userId, hash, ttl, expiresAt)
        return Session(token, ttl.inWholeSeconds.toInt(), now, expiresAt)
    }

    private suspend fun getUsernameProblem(username: Username): Problem? {
        val outcome = username.toValidOutcome()
        if (outcome is Problem) return outcome
        val id = service.readIdByUsername(username)
        if (id != null) return Problem("Username already exists.")
        return null
    }

    private suspend fun getEmailProblem(email: Email?): Problem? {
        val email = email ?: return null // email is optional
        val outcome = email.toValidOutcome()
        if (outcome is Problem) return outcome
        val user = service.readByUsernameOrEmail(email)
        if (user != null) return Problem("Email already exists.")
        return null
    }

    private fun getPasswordProblem(password: Password): Problem? {
        val outcome = password.toValidOutcome()
        if (outcome is Problem) return outcome
        return null
    }
}

// fun hashPassword(password: Password, salt: ByteArray): HashedPassword {
//     val iterations = 65536
//     val keyLength = 256
//     val spec = PBEKeySpec(password.value.toCharArray(), salt, iterations, keyLength)
//     val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//     val hash = factory.generateSecret(spec).encoded
//     return HashedPassword(Base64.getEncoder().encodeToString(hash))
// }

fun hashPassword(password: Password): HashedPassword {
    val hash = BCrypt.withDefaults().hashToString(10, password.value.toCharArray())
    return HashedPassword(hash)
}

fun generateSalt(): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}

private fun String.base64ToByteArray(): ByteArray {
    return Base64.getDecoder().decode(this)
}

class InvalidLoginException(reason: String) : Exception(reason)

fun generateToken(): Token {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Token(bytes.toBase64Url())
}

fun ByteArray.toBase64Url(): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(this)

fun ByteArray.toHex(): String =
    joinToString("") { "%02x".format(it) }

fun hashToken(token: Token): HashedToken = MessageDigest.getInstance("SHA-256")
    .digest(token.value.toByteArray())
    .toHex().let { HashedToken(it) }

val TOKEN_DEFAULT_TTL = 30.days
val TOKEN_TEMP_TTL = 6.hours
val GUEST_TOKEN_TTL = 400.days // does not reflect activity period policy
val GUEST_ACTIVITY_PERIOD = 30.days
const val SESSION_COOKIE_NAME = "session_token"
const val GUEST_COOKIE_NAME = "guest_token"