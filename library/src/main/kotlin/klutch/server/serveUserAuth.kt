package klutch.server

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.GMTDate
import kampfire.api.UserApi
import kabinet.console.globalConsole
import kampfire.api.Username
import kampfire.model.Auth
import kampfire.model.AuthUser
import kampfire.model.Ok
import kampfire.model.SignUpResult
import kampfire.model.responseOf
import kampfire.model.toResponse
import klutch.db.services.AuthDao
import klutch.db.services.AuthId
import klutch.db.services.AuthService
import klutch.db.services.RefreshTokenService

private val console = globalConsole.getHandle("serveUsers")

fun <User: AuthUser, Id: AuthId> ApiContext.serveUserAuth(
    dao: AuthDao<User, Id>,
    authGate: (Boolean, Route.() -> Unit) -> Unit,
    getUsername: (RoutingCall) -> Username,
    getUserId: (RoutingCall) -> Id
) {
    val refreshTokenService = server.get<RefreshTokenService>()
    val jwtService = server.get<JwtService>()
    val authorizer = Authorizer(refreshTokenService, jwtService, dao::readByUsernameOrEmail)
    val authService = AuthService(dao)
    val usernameGenerator = UsernameGenerator()

    postApi(UserApi.Create) {
        try {
            val authId = authService.createUser(it.data)
            val auth = authorizer.authorizeNewAccount(authId, it.data.stayLoggedIn)
            call.appendCookies(auth)
            true
        } catch (e: IllegalArgumentException) {
            console.logError("serveUsers.createUser fail: ${e.message}")
            false
        }.toResponse()
    }

    getApi(UserApi.GenerateUsername) {
        Ok(usernameGenerator.generate())
    }

    postApi(UserApi.CheckUsername) {
        Ok(dao.readByUsernameOrEmail(it.data.value) == null)
    }

    post(UserApi.Refresh.path) {
        console.log("refreshing")
        val refreshToken = call.request.cookies["refresh_token"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized)
        val auth = authorizer.authorize(refreshToken)
        call.appendCookies(auth)
        call.respond(HttpStatusCode.OK)
    }

    postApi(UserApi.Login) {
        try {
            console.log("logging in")
            val auth = authorizer.authorize(it.data)
            call.appendCookies(auth)
            true
        } catch (e: InvalidLoginException) {
            console.log("Invalid login: ${it.data.usernameOrEmail}")
            call.respond(HttpStatusCode.Unauthorized, e.message ?: "Invalid login attempt")
            false
        }.toResponse()
    }

    authGate(true) {
        post(UserApi.Logout.path) {
            val userId = getUserId(call)
            refreshTokenService.deleteTokens(userId)
            call.response.cookies.append(
                Cookie(
                    name = "auth_token",
                    value = "",
                    httpOnly = true,
                    secure = true,
                    path = "/",
                    expires = GMTDate.START,
                    extensions = mapOf("SameSite" to "Strict")
                )
            )
            call.response.cookies.append(
                Cookie(
                    name = "refresh_token",
                    value = "",
                    httpOnly = true,
                    secure = true,
                    path = UserApi.Refresh.path,
                    expires = GMTDate.START,
                    extensions = mapOf("SameSite" to "Strict")
                )
            )
            call.respond(HttpStatusCode.OK)
        }

        getApi(UserApi.Private) {
            val username = getUsername(call)
            responseOf(dao.readPrivateInfo(username.value))
        }
    }
}

private fun RoutingCall.appendCookies(auth: Auth) {
    response.cookies.append(
        Cookie(
            name = "auth_token",
            value = auth.jwt.value,
            httpOnly = true,
            secure = true,
            path = "/",
            maxAge = auth.jwt.maxAgeSeconds,
            extensions = mapOf("SameSite" to "Strict")
        )
    )
    response.cookies.append(
        Cookie(
            name = "refresh_token",
            value = auth.refreshToken.value,
            httpOnly = true,
            secure = true,
            path = UserApi.Refresh.path,
            maxAge = auth.refreshToken.maxAgeSeconds,
            extensions = mapOf("SameSite" to "Strict")
        )
    )
}