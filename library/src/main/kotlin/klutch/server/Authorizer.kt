package klutch.server

import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.api.HashedPassword
import kampfire.api.Password
import kampfire.api.TableId
import kampfire.api.Username
import kampfire.api.aboveMinScore
import kampfire.api.toLoginIdentity
import kampfire.api.toValidOutcome
import kampfire.api.validUsernameChars
import kampfire.api.validUsernameLength
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
import kampfire.model.toOutcome
import kampfire.utils.deobfuscate
import kampfire.utils.printTimedValue
import klutch.db.services.AuthId
import klutch.db.services.SessionService
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.measureTimedValue
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger("authorize")

class Authorizer(
    val service: SessionService
) {
    suspend fun authorize(
        loginRequest: LoginRequest,
    ): Outcome<Session> {

        val claimedUser = service.readByUsernameOrEmail(loginRequest.loginIdentity.toLoginIdentity())
        if (claimedUser == null) {
            log.debug { "authorize: Invalid username" }
            return Problem("Invalid username")
        }
        loginRequest.password?.let {
            val givenPassword = it.deobfuscate()
            val session = testPassword(claimedUser, givenPassword, loginRequest.isTemp)
            if (session == null) {
                log.debug { "authorize: Invalid password attempt" }
                return Problem("Invalid password")
            }
            log.debug { "authorize: password login" }
            return Ok(session)
        }

        return Problem("Missing password and token")
    }

    suspend fun authorizeNewAccount(userId: AuthId, stayLoggedIn: Boolean): Session {
        return createSession(userId, stayLoggedIn)
    }

    suspend fun testPassword(
        claimedUser: UserRecord,
        givenPassword: Password,
        stayLoggedIn: Boolean,
    ): Session? {
        if (!verifyPassword(givenPassword, claimedUser.hashedPassword)) {
            log.info { "Invalid password attempt" }
            return null
        }

        return createSession(claimedUser.userId, stayLoggedIn)
    }

    fun verifyPassword(password: Password, stored: HashedPassword): Boolean =
        BCrypt.verifyer().verify(password.value.toCharArray(), stored.value.toCharArray()).verified

//    suspend fun testToken(refreshToken: String): AuthLegacy? {
//        val cachedToken = tokenService.readToken(refreshToken)
//            ?: return null
//        if (cachedToken.isExpired) {
//            tokenService.deleteToken(refreshToken)
//            return null
//        }
//        val returnedToken = if (cachedToken.needsRotating) {
//            tokenService.deleteToken(refreshToken)
//            createSession(cachedToken.userId, true)
//        } else {
//            TokenInfo(refreshToken, (cachedToken.expiresAt - Clock.System.now()).inWholeSeconds.toInt())
//        }
//        val jwt = jwtService.createAccessToken(cachedToken.userId.value)
//        return AuthLegacy(jwt, returnedToken)
//    }

    suspend fun createUser(
        request: SignUpRequest,
        roles: Set<UserRole> = setOf(UserRole.User),
    ): Outcome<TableId<Uuid>> {
        log.info { "Creating user" }

        val problem = getUsernameProblem(request) ?: getEmailProblem(request) ?: getPasswordProblem(request)
        if (problem != null) return problem.also {
            log.info { "Create user problem: ${it.message}" }
        }

        val hashedPassword = hashPassword(request.password)
        val seed = UserSeed(
            request = request,
            hashedPassword = hashedPassword,
            roles = roles,
            accountType = request.accountType
        )

        return Ok(service.createUserRecord(seed))
    }

    private suspend fun createSession(
        userId: TableId<Uuid>,
        isTemp: Boolean,
    ): Session {
        val token = generateSessionToken()
        val hash = hashToken(token)
        val ttl = when(isTemp) {
            true -> TOKEN_TEMP_TTL
            false -> TOKEN_DEFAULT_TTL
        }
        val expiresAt = Clock.System.now() + ttl
        service.createSessionRecord(userId, hash, ttl, expiresAt)
        return Session(token, ttl.inWholeSeconds.toInt(), expiresAt)
    }

    private suspend fun getUsernameProblem(info: SignUpRequest): Problem? {
        val outcome = info.username.toValidOutcome()
        if (outcome is Problem) return outcome
        val id = service.readIdByUsername(info.username)
        if (id != null) return Problem("Username already exists.")
        return null
    }

    private suspend fun getEmailProblem(info: SignUpRequest): Problem? {
        val email = info.email ?: return null // email is optional
        val outcome = email.toValidOutcome()
        if (outcome is Problem) return outcome
        val user = service.readByUsernameOrEmail(email)
        if (user != null) return Problem("Email already exists.")
        return null
    }

    private fun getPasswordProblem(info: SignUpRequest): Problem? {
        val outcome = info.password.toValidOutcome()
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

fun generateSessionToken(): Token {
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
const val SESSION_COOKIE_NAME = "session_token"