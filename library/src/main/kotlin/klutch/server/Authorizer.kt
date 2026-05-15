package klutch.server

import kabinet.console.globalConsole
import kampfire.api.TableId
import kampfire.model.Auth
import kampfire.model.AuthUser
import kampfire.model.LoginRequest
import kampfire.model.Token
import kampfire.utils.deobfuscate
import klutch.db.services.RefreshTokenService
import klutch.db.tables.RefreshTokenTable
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.time.Clock
import kotlin.uuid.Uuid

private val console = globalConsole.getHandle("authorize")

class Authorizer(
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JwtService,
    private val readByUsernameOrEmail: suspend (String) -> AuthUser?,
) {

    suspend fun authorize(
        loginRequest: LoginRequest,
    ): Auth {

        val claimedUser = readByUsernameOrEmail(loginRequest.usernameOrEmail)
        if (claimedUser == null) {
            console.logInfo("authorize: Invalid username from ${loginRequest.usernameOrEmail}")
            throw InvalidLoginException("Invalid username")
        }
        loginRequest.password?.let {
            val givenPassword = it.deobfuscate()
            val authInfo = testPassword(claimedUser, givenPassword, loginRequest.stayLoggedIn)
            if (authInfo == null) {
                console.logInfo("authorize: Invalid password attempt from ${loginRequest.usernameOrEmail}")
                throw InvalidLoginException("Invalid password")
            }
            console.logInfo("authorize: password login by ${loginRequest.usernameOrEmail}")
            return authInfo
        }

        throw InvalidLoginException("Missing password and token")
    }

    suspend fun authorize(
        refreshToken: String,
    ): Auth {
        val authInfo = testToken(refreshToken)
        if (authInfo == null) {
            console.logInfo("authorize: Invalid token attempt")
            throw InvalidLoginException("Invalid token")
        }
        console.logDebug("authorize: token login")
        return authInfo
    }

    suspend fun testPassword(
        claimedUser: AuthUser,
        givenPassword: String,
        stayLoggedIn: Boolean,
    ): Auth? {
        val byteArray = claimedUser.salt.base64ToByteArray()
        val hashedPassword = hashPassword(givenPassword, byteArray)
        if (hashedPassword != claimedUser.hashedPassword) {
            return null
        }

        val sessionToken = createRefreshToken(claimedUser.userId, stayLoggedIn)
        val jwt = jwtService.createAccessToken(claimedUser.userId.value)
        return Auth(jwt, sessionToken)
    }

    suspend fun testToken(refreshToken: String): Auth? {
        val cachedToken = refreshTokenService.readToken(refreshToken)
            ?: return null
        if (cachedToken.isExpired) {
            refreshTokenService.deleteToken(refreshToken)
            return null
        }
        val returnedToken = if (cachedToken.needsRotating) {
            refreshTokenService.deleteToken(refreshToken)
            createRefreshToken(cachedToken.userId, true)
        } else {
            Token(refreshToken, (cachedToken.expiresAt - Clock.System.now()).inWholeSeconds.toInt())
        }
        val jwt = jwtService.createAccessToken(cachedToken.userId.value)
        return Auth(jwt, returnedToken)
    }

    private suspend fun createRefreshToken(
        userId: TableId<Uuid>,
        stayLoggedIn: Boolean,
    ): Token {
        return refreshTokenService.createToken(userId, generateTokenString(), stayLoggedIn)
    }
}

private fun generateTokenString() = UUID.randomUUID().toString()

fun hashPassword(password: String, salt: ByteArray): String {
    val iterations = 65536
    val keyLength = 256
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val hash = factory.generateSecret(spec).encoded
    return Base64.getEncoder().encodeToString(hash)
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
