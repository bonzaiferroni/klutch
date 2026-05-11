package klutch.server

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kampfire.api.UserApi
import kabinet.console.globalConsole
import kampfire.api.TableId
import kampfire.model.Auth
import kampfire.model.AuthUser
import kampfire.model.SignUpResult
import kampfire.model.Token
import kampfire.model.UserSeed
import klutch.db.services.AuthDao
import klutch.db.services.AuthId
import klutch.db.services.AuthService
import klutch.db.tables.RefreshTokenTable

private val console = globalConsole.getHandle("serveUsers")

fun <User: AuthUser, Id: AuthId> Routing.serveUserAuth(
    dao: AuthDao<User, Id>,
    refreshTokenTable: RefreshTokenTable,
    createToken: (String) -> Token,
    authGate: (Boolean, Route.() -> Unit) -> Unit,
    getUsername: (RoutingCall) -> String
) {
    val authorizer = Authorizer(refreshTokenTable, dao::readByUsernameOrEmail, createToken)
    val service = AuthService(dao)

    postEndpoint(UserApi.Create) {
        try {
            service.createUser(it.data)
            SignUpResult(true, "User created.")
        } catch (e: IllegalArgumentException) {
            console.logError("serveUsers.createUser fail: ${e.message}")
            SignUpResult(false, e.message.toString())
        }
    }

    post(UserApi.Refresh.path) {
        val refreshToken = call.request.cookies["refresh_token"]
            ?: return@post call.respond(HttpStatusCode.Unauthorized)
        val auth = authorizer.authorize(refreshToken)
        call.appendCookies(auth)
        call.respond(HttpStatusCode.OK)
    }

    postEndpoint(UserApi.Login) {
        try {
            val auth = authorizer.authorize(it.data)
            call.appendCookies(auth)
            call.respond(HttpStatusCode.OK)
        } catch (e: InvalidLoginException) {
            console.log("Invalid login: ${it.data.usernameOrEmail}")
            call.respond(HttpStatusCode.Unauthorized, e.message ?: "Invalid login attempt")
            null
        }
    }

    authGate(true) {
//        getEndpoint(UserApi.ReadInfo) {
//            val username = getUsername()
//            dao.readUserInfo(username)
//        }

        getEndpoint(UserApi.Private) {
            val username = getUsername(call)
            dao.readPrivateInfo(username)
        }

//        postEndpoint(UserApi.Update) {
//            val userId = getUserId()
//            dao.updateUser(it.data, userId)
//        }

//        put(UserApi.Users.Update) {
//            val username = call.getClaim(CLAIM_USERNAME)
//            val info = call.receive<EditUserRequest>()
//            service.updateUser(username, info)
//            call.respond(HttpStatusCode.OK, true)
//        }
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