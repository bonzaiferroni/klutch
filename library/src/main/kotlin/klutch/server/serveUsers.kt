package klutch.server

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kampfire.api.UserApi
import kabinet.console.globalConsole
import klutch.utils.getClaim
import kampfire.model.SignUpResult
import klutch.db.services.UserTableService

private val console = globalConsole.getHandle("serveUsers")

fun Routing.serveUsers(service: UserTableService = UserTableService()) {

    postEndpoint(UserApi.Create) { request, endpoint ->
        try {
            service.createUser(request)
            SignUpResult(true, "User created.")
        } catch (e: IllegalArgumentException) {
            console.logError("serveUsers.createUser fail: ${e.message}")
            SignUpResult(false, e.message.toString())
        }
    }

    postEndpoint(UserApi.Login) { request, endpoint ->
        try {
            call.authorize(request)
        } catch (e: InvalidLoginException) {
            console.log("Invalid login: ${request.usernameOrEmail}")
            call.respond(HttpStatusCode.Unauthorized, e.message ?: "Invalid login attempt")
            null
        }
    }

    authenticateJwt {
        getEndpoint(UserApi.ReadInfo) {
            val username = getClaim(CLAIM_USERNAME)
            service.readUserDto(username)
        }

        getEndpoint(UserApi.UserInfo) {
            val username = getClaim(CLAIM_USERNAME)
            service.getPrivateInfo(username)
        }

//        put(UserApi.Users.Update) {
//            val username = call.getClaim(CLAIM_USERNAME)
//            val info = call.receive<EditUserRequest>()
//            service.updateUser(username, info)
//            call.respond(HttpStatusCode.OK, true)
//        }
    }
}