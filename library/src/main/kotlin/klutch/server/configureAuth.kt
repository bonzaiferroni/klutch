package klutch.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.util.date.GMTDate
import kampfire.api.UserApi
import kampfire.model.Session
import kampfire.model.SessionIdentity
import kampfire.model.Token
import klutch.db.services.SessionService
import kotlinx.coroutines.launch

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
        if (principal.session.activityRefreshDue()) {
            application.launch {
                runCatching { service.refreshActivity(principal.identity.callerId) }
                    .onFailure { log.warn(it) { "Failed to refresh activity" } }
            }
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

fun ApplicationCall.appendGuestCooke(token: Token?) {
    when (token) {
        null -> response.cookies.append(
            Cookie(
                name = GUEST_COOKIE_NAME,
                value = "",
                httpOnly = true,
                secure = true,
                path = UserApi.Login.path,
                expires = GMTDate.START,
                extensions = mapOf("SameSite" to "Strict")
            )
        )
        else -> response.cookies.append(
            Cookie(
                name = GUEST_COOKIE_NAME,
                value = token.value,
                httpOnly = true,
                secure = true,
                path = UserApi.Login.path,
                maxAge = GUEST_TOKEN_TTL.inWholeSeconds.toInt(),
                extensions = mapOf("SameSite" to "Strict")
            )
        )
    }
}
