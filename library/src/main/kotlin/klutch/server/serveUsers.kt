package klutch.server

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kampfire.api.UserApi
import kabinet.console.globalConsole
import kampfire.model.SignUpResult
import klutch.db.services.UserTableService
import klutch.utils.getUserId
import klutch.utils.getUsername

private val console = globalConsole.getHandle("serveUsers")

fun Routing.serveUsers(service: UserTableService = UserTableService()) {

    postEndpoint(UserApi.Create) {
        try {
            service.createUser(it.data)
            SignUpResult(true, "User created.")
        } catch (e: IllegalArgumentException) {
            console.logError("serveUsers.createUser fail: ${e.message}")
            SignUpResult(false, e.message.toString())
        }
    }

    postEndpoint(UserApi.Login) {
        try {
            call.authorize(it.data)
        } catch (e: InvalidLoginException) {
            console.log("Invalid login: ${it.data.usernameOrEmail}")
            call.respond(HttpStatusCode.Unauthorized, e.message ?: "Invalid login attempt")
            null
        }
    }

    authenticateJwt {
        getEndpoint(UserApi.ReadInfo) {
            val username = getUsername()
            service.readUserInfo(username)
        }

        getEndpoint(UserApi.Private) {
            val username = getUsername()
            service.getPrivateInfo(username)
        }

        postEndpoint(UserApi.Update) {
            val userId = getUserId()
            service.updateUser(it.data, userId)
        }

//        put(UserApi.Users.Update) {
//            val username = call.getClaim(CLAIM_USERNAME)
//            val info = call.receive<EditUserRequest>()
//            service.updateUser(username, info)
//            call.respond(HttpStatusCode.OK, true)
//        }
    }
}