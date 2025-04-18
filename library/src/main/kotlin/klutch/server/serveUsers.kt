package klutch.server

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kabinet.api.UserApi
import kabinet.model.EditUserRequest
import kabinet.model.SignUpRequest
import klutch.utils.getClaim
import kabinet.model.SignUpResult
import klutch.db.services.UserDtoService

fun Routing.serveUsers(service: UserDtoService = UserDtoService()) {

    post(UserApi.Users.GetUser.path) {
        val info = call.receive<SignUpRequest>()
        try {
            service.createUser(info)
        } catch (e: IllegalArgumentException) {
            println("serveUsers.createUser: ${e.message}")
            call.respond(HttpStatusCode.OK, SignUpResult(false, e.message.toString()))
            return@post
        }
        call.respond(status = HttpStatusCode.OK, SignUpResult(true, "User created."))
    }

    authenticateJwt {
        get(UserApi.Users.GetUser.path) {
            val username = call.getClaim(CLAIM_USERNAME)
            val userInfo = service.readUserDto(username)
            call.respond(userInfo)
        }

        get(UserApi.Users.GetPrivateInfo) {
            val username = call.getClaim(CLAIM_USERNAME)
            service.getPrivateInfo(username)
        }

        put(UserApi.Users.GetUser.path) {
            val username = call.getClaim(CLAIM_USERNAME)
            val info = call.receive<EditUserRequest>()
            service.updateUser(username, info)
            call.respond(HttpStatusCode.OK, true)
        }
    }
}