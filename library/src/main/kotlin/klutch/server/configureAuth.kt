package klutch.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.intercept
import io.ktor.util.date.GMTDate
import kampfire.model.Session
import kampfire.model.SessionIdentity
import kampfire.model.Token
import klutch.db.services.SessionService

private val log = KotlinLogging.logger(Application::configureAuth.name)

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
                val token = Token(credential.token)
                service.readSessionIdentity(token)
            }
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<SessionIdentity>() ?: return@intercept
        if (principal.session.pastHalfLife()) {
            val session = service.extendSession(principal.session)
            call.appendSessionCookie(session)
        }
    }

    install(StatusPages) {
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.appendSessionCookie(null)
            if (!call.request.path().startsWith("/api")) {
                call.respondRedirect("/")
            }
        }
    }
}

fun Route.authGate(optional: Boolean = false, block: Route.() -> Unit) = authenticate(TOKEN_NAME, optional = optional) {
    block()
}

const val TOKEN_NAME = "auth-token"

fun ApplicationCall.appendSessionCookie(session: Session?) {
    when (session) {
        null -> response.cookies.append(
            Cookie(
                name = SESSION_COOKIE_NAME,
                value = "",
                httpOnly = true,
                secure = true,
                path = "/",
                expires = GMTDate.START,
                extensions = mapOf("SameSite" to "Strict")
            )
        )

        else -> response.cookies.append(
            Cookie(
                name = SESSION_COOKIE_NAME,
                value = session.token.value,
                httpOnly = true,
                secure = true,
                path = "/",
                maxAge = session.ttlSeconds,
                extensions = mapOf("SameSite" to "Strict")
            )
        )
    }
}

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