package klutch.server

import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.model.AuthLegacy
import kampfire.model.AuthUser
import kampfire.model.LoginRequest
import kampfire.model.TokenInfo
import kampfire.utils.deobfuscate
import klutch.db.services.AuthId
import java.util.*
import kotlin.time.Clock

private val console = KotlinLogging.logger("authorize")

//class AuthorizerLegacy(
//    private val refreshTokenService: RefreshTokenService,
//    private val jwtService: JwtService,
//    private val readByUsernameOrEmail: suspend (String) -> AuthUser?,
//) {
//
//    suspend fun authorize(
//        loginRequest: LoginRequest,
//    ): AuthLegacy {
//
//        val claimedUser = readByUsernameOrEmail(loginRequest.usernameOrEmail)
//        if (claimedUser == null) {
//            console.info { "authorize: Invalid username from ${loginRequest.usernameOrEmail}" }
//            throw InvalidLoginException("Invalid username")
//        }
//        loginRequest.password?.let {
//            val givenPassword = it.deobfuscate()
//            val authInfo = testPassword(claimedUser, givenPassword, loginRequest.isTemp)
//            if (authInfo == null) {
//                console.info { "authorize: Invalid password attempt from ${loginRequest.usernameOrEmail}" }
//                throw InvalidLoginException("Invalid password")
//            }
//            console.info { "authorize: password login by ${loginRequest.usernameOrEmail}" }
//            return authInfo
//        }
//
//        throw InvalidLoginException("Missing password and token")
//    }
//
//    suspend fun authorize(
//        refreshToken: String,
//    ): AuthLegacy {
//        val authInfo = testToken(refreshToken)
//        if (authInfo == null) {
//            console.info { "authorize: Invalid token attempt"}
//            throw InvalidLoginException("Invalid token")
//        }
//        console.debug { "authorize: token login" }
//        return authInfo
//    }
//
//    suspend fun authorizeNewAccount(userId: AuthId, stayLoggedIn: Boolean): AuthLegacy {
//        val jwt = jwtService.createAccessToken(userId.value)
//        val refreshToken = createRefreshToken(userId, stayLoggedIn)
//        return AuthLegacy(jwt, refreshToken)
//    }
//
//    suspend fun testPassword(
//        claimedUser: AuthUser,
//        givenPassword: String,
//        stayLoggedIn: Boolean,
//    ): AuthLegacy? {
//        val byteArray = claimedUser.salt.base64ToByteArray()
//        val hashedPassword = hashPassword(givenPassword, byteArray)
//        if (hashedPassword != claimedUser.hashedPassword) {
//            return null
//        }
//
//        val sessionToken = createRefreshToken(claimedUser.userId, stayLoggedIn)
//        val jwt = jwtService.createAccessToken(claimedUser.userId.value)
//        return AuthLegacy(jwt, sessionToken)
//    }
//
//    suspend fun testToken(refreshToken: String): AuthLegacy? {
//        val cachedToken = refreshTokenService.readToken(refreshToken)
//            ?: return null
//        if (cachedToken.isExpired) {
//            refreshTokenService.deleteToken(refreshToken)
//            return null
//        }
//        val returnedToken = if (cachedToken.needsRotating) {
//            refreshTokenService.deleteToken(refreshToken)
//            createRefreshToken(cachedToken.userId, true)
//        } else {
//            TokenInfo(refreshToken, (cachedToken.expiresAt - Clock.System.now()).inWholeSeconds.toInt())
//        }
//        val jwt = jwtService.createAccessToken(cachedToken.userId.value)
//        return AuthLegacy(jwt, returnedToken)
//    }
//
//    private suspend fun createRefreshToken(
//        userId: AuthId,
//        stayLoggedIn: Boolean,
//    ): TokenInfo {
//        return refreshTokenService.createToken(userId, generateTokenString(), stayLoggedIn)
//    }
//}
//
//private fun generateTokenString() = UUID.randomUUID().toString()
//
//private fun String.base64ToByteArray(): ByteArray {
//    return Base64.getDecoder().decode(this)
//}
