package klutch.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kabinet.console.globalConsole
import klutch.db.model.User
import kabinet.model.LoginRequest
import klutch.db.tables.UserTable
import kabinet.model.Auth
import kabinet.utils.deobfuscate
import klutch.db.services.RefreshTokenService
import klutch.db.services.UserApiService
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val console = globalConsole.getHandle("authorize")

suspend fun ApplicationCall.authorize(loginRequest: LoginRequest, service: UserApiService = UserApiService()): Auth? {

    val claimedUser = service.readByUsernameOrEmail(loginRequest.usernameOrEmail)
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
    loginRequest.refreshToken?.let {
        val authInfo = testToken(claimedUser, it, loginRequest.stayLoggedIn)
        if (authInfo == null) {
            console.logInfo("authorize: Invalid password attempt from ${loginRequest.usernameOrEmail}")
            throw InvalidLoginException("Invalid token")
        }
        console.logDebug("authorize: session login by ${loginRequest.usernameOrEmail}")
        this.respond(HttpStatusCode.OK, authInfo)
        return authInfo
    }
    throw InvalidLoginException("Missing password and token")
}

suspend fun testPassword(claimedUser: User, givenPassword: String, stayLoggedIn: Boolean): Auth? {
    val byteArray = claimedUser.salt.base64ToByteArray()
    val hashedPassword = hashPassword(givenPassword, byteArray)
    if (hashedPassword != claimedUser.hashedPassword) {
        return null
    }

    val sessionToken = createRefreshToken(claimedUser, stayLoggedIn)
    val jwt = createJWT(claimedUser.username, claimedUser.roles)
    return Auth(jwt, sessionToken)
}

suspend fun testToken(claimedUser: User, refreshToken: String, stayLoggedIn: Boolean): Auth? {
    val service = RefreshTokenService()
    val cachedToken = service.readToken(refreshToken)
        ?: return null
    if (cachedToken.userId != claimedUser.userId) {
        return null
    }
    if (cachedToken.isExpired) {
        service.deleteToken(refreshToken)
        return null
    }
    val returnedToken = if (cachedToken.needsRotating) {
        service.deleteToken(refreshToken)
        createRefreshToken(claimedUser, stayLoggedIn)
    } else {
        refreshToken
    }
    val jwt = createJWT(claimedUser.username, claimedUser.roles)
    return Auth(jwt, returnedToken)
}

fun generateToken() = UUID.randomUUID().toString()

suspend fun createRefreshToken(user: User, stayLoggedIn: Boolean): String {
    val service = RefreshTokenService()
    val generatedToken = generateToken()
    service.createToken(user.userId, generatedToken, stayLoggedIn)
    return generatedToken
}

fun hashPassword(password: String, salt: ByteArray): String {
    val iterations = 65536
    val keyLength = 256
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val hash = factory.generateSecret(spec).encoded
    return Base64.getEncoder().encodeToString(hash)
}

fun generateUniqueSalt(): ByteArray {
    while (true) {
        val salt = generateSalt()
        val isUnique = UserTable
            .select(UserTable.salt)
            .where { UserTable.salt.eq(salt.toBase64()) }
            .toList().isEmpty()
        if (isUnique) return salt
    }
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

fun String.base64ToByteArray(): ByteArray {
    return Base64.getDecoder().decode(this)
}

class InvalidLoginException(reason: String) : Exception(reason)
