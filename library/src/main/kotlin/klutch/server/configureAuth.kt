package klutch.server

import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import io.ktor.server.routing.Route
import kampfire.model.Token
import klutch.db.services.SessionService

fun Application.configureAuth(
    service: SessionService
) {
    authentication {
        bearer(TOKEN_NAME) {
            authHeader { call ->
                call.request.cookies[SESSION_COOKIE_NAME]?.let {
                    HttpAuthHeader.Single("Bearer", it)
                }
            }
            authenticate { credential ->
                val hash = hashToken(Token(credential.token))
                val sessionPrincipal = service.readSessionPrincipal(hash)
                // td: refresh token at half life
                sessionPrincipal?.principal
            }
        }
    }
}

fun Route.authGate(optional: Boolean = false, block: Route.() -> Unit) = authenticate(TOKEN_NAME, optional = optional) {
    block()
}

const val TOKEN_NAME = "auth-token"

// class TokenConfig(
//     val audience: String,
//     val issuer: String,
//     val realm: String,
// )
// const val APP_SECRET_KEY = "APP_SECRET"

//fun Application.configureAuth(scope: ProviderScope, principalOf: suspend (Uuid) -> Any?) {
//    val env = scope.provide<Environment>()
//    val config = scope.provide<TokenConfig>()
//    val secret = env.read(APP_SECRET_KEY)
//
//    authentication {
//        jwt(TOKEN_NAME) {
//            realm = config.realm
//            authHeader { call ->
//                call.request.cookies["auth_token"]?.let {
//                    HttpAuthHeader.Single("Bearer", it)
//                }
//            }
//            verifier(
//                JWT
//                    .require(Algorithm.HMAC256(secret))
//                    .withAudience(config.audience)
//                    .withIssuer(config.issuer)
//                    .withClaimPresence("sub")
//                    .build()
//            )
//            validate { credential ->
//                credential.payload.subject?.let {
//                    principalOf(Uuid.parse(it))
//                }
//            }
//            challenge { _, _ ->
//                serverLog.logDebug("Security: JWT authentication failed")
//                call.respond(HttpStatusCode.Unauthorized)
//            }
//        }
//    }
//}



// class JwtService(
//     private val env: Environment,
//     private val config: TokenConfig,
// ) {
//     fun createAccessToken(userId: Uuid): TokenInfo {
//         val secret = env.read(APP_SECRET_KEY)
//         val value = JWT.create()
//             .withAudience(config.audience)
//             .withIssuer(config.issuer)
//             .withExpiresAt(Date(System.currentTimeMillis() + config.lifetimeSeconds * 1000)) // 30 minutes
//             .withSubject(userId.toString())
//             .sign(Algorithm.HMAC256(secret))
//         return TokenInfo(value, config.lifetimeSeconds)
//     }
// }

// private fun generateSecret(): String {
//     val bytes = ByteArray(64)
//     SecureRandom().nextBytes(bytes)
//     return Base64.getEncoder().encodeToString(bytes)
// }