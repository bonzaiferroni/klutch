package klutch.server

import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kabinet.api.UserApi
import kabinet.model.EditUserRequest
import kabinet.model.SignUpRequest
import klutch.utils.getClaim
import kabinet.model.SignUpResult
import klutch.db.services.UserApiService

fun Routing.serveUsers(service: UserApiService = UserApiService()) {

    post(UserApi.Create) { request, endpoint ->
        try {
            service.createUser(request)
            SignUpResult(true, "User created.")
        } catch (e: IllegalArgumentException) {
            println("serveUsers.createUser fail: ${e.message}")
            SignUpResult(false, e.message.toString())
        }
    }

    post(UserApi.Login) { request, endpoint ->
        try {
            call.authorize(request)
        } catch (e: InvalidLoginException) {
            call.respond(HttpStatusCode.Unauthorized, e.message ?: "Invalid login attempt")
            null
        }
    }

    authenticateJwt {
        get(UserApi.ReadInfo) {
            val username = call.getClaim(CLAIM_USERNAME)
            service.readUserDto(username)
        }

        get(UserApi.PrivateInfo) {
            val username = call.getClaim(CLAIM_USERNAME)
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