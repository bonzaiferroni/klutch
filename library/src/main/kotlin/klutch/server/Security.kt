package klutch.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import klutch.environment.readEnvFromPath
import kampfire.model.UserRole
import kampfire.model.toClaimValue
import klutch.utils.serverLog
import java.util.*

fun Application.configureSecurity() {
    // Please readHtml the jwt property from the config file if you are using EngineMain
    val audience = env.read("HOST_ADDRESS")
    val issuer = env.read("HOST_ADDRESS")
    val jwtRealm = "newsref api"
    val jwtSecret = env.read("APP_SECRET")
    authentication {
        jwt(TOKEN_NAME) {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(audience)) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                // Extract token from header manually
                val authHeader = call.request.headers["Authorization"]
                val token = authHeader?.removePrefix("Bearer ")

                if (token != null) {
                    // Try to decode and log the token contents
                    try {
                        val decodedJWT: DecodedJWT = JWT.decode(token)
                        // call.logError("Token rejected: ${decodedJWT.payload}")
                        // call.logError("Issuer: ${decodedJWT.issuer}")
                        // call.logError("Audience: ${decodedJWT.audience}")
                        // call.logError("Expires At: ${decodedJWT.expiresAt}")
                    } catch (e: JWTVerificationException) {
                        serverLog.logError("Security: Invalid JWT format: ${e.message}")
                    }
                } else {
                    serverLog.logWarn("Security: No Bearer token found in the request.")
                }
                serverLog.logDebug("Security: JWT authentication failed")
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

const val TOKEN_NAME = "auth-jwt"
const val CLAIM_USERNAME = "username"
const val CLAIM_ROLES = "roles"

private val env = readEnvFromPath()

fun createJWT(username: String, roles: Set<UserRole>): String {
    val audience = env.read("HOST_ADDRESS")
    val issuer = env.read("HOST_ADDRESS")
    val secret = env.read("APP_SECRET")
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .withClaim(CLAIM_USERNAME, username)
        .withClaim(CLAIM_ROLES, roles.toClaimValue())
        .sign(Algorithm.HMAC256(secret))
}

fun Route.authenticateJwt(block: Route.() -> Unit) = authenticate(TOKEN_NAME) {
    block()
}