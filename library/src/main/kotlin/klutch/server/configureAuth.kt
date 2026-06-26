package klutch.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kabinet.utils.Environment
import kampfire.model.Token
import klutch.utils.serverLog
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import kotlin.uuid.Uuid

fun Application.configureAuth(scope: ProviderScope, principalOf: suspend (Uuid) -> Any?) {
    val env = scope.provide<Environment>()
    val config = scope.provide<TokenConfig>()
    val secret = env.read(APP_SECRET_KEY)

    authentication {
        jwt(TOKEN_NAME) {
            realm = config.realm
            authHeader { call ->
                call.request.cookies["auth_token"]?.let {
                    HttpAuthHeader.Single("Bearer", it)
                }
            }
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .withClaimPresence("sub")
                    .build()
            )
            validate { credential ->
                credential.payload.subject?.let {
                    principalOf(Uuid.parse(it))
                }
            }
            challenge { _, _ ->
                serverLog.logDebug("Security: JWT authentication failed")
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

class TokenConfig(
    val audience: String,
    val issuer: String,
    val realm: String,
    val lifetimeSeconds: Int
)

const val TOKEN_NAME = "auth-jwt"
const val APP_SECRET_KEY = "APP_SECRET"

class JwtService(
    private val env: Environment,
    private val config: TokenConfig,
) {
    fun createAccessToken(userId: Uuid): Token {
        val secret = env.read(APP_SECRET_KEY)
        val value = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.lifetimeSeconds * 1000)) // 30 minutes
            .withSubject(userId.toString())
            .sign(Algorithm.HMAC256(secret))
        return Token(value, config.lifetimeSeconds)
    }
}

fun Route.authGate(optional: Boolean = false, block: Route.() -> Unit) = authenticate(TOKEN_NAME, optional = optional) {
    block()
}

private fun generateSecret(): String {
    val bytes = ByteArray(64)
    SecureRandom().nextBytes(bytes)
    return Base64.getEncoder().encodeToString(bytes)
}