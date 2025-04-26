package klutch.db

import kabinet.model.SignUpRequest
import klutch.db.services.UserApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


suspend fun initUsers() {
    val userFile = File("users.json")
    if (!userFile.exists()) return
    val users = Json.decodeFromString<List<InitialUser>>(userFile.readText())
    for (user in users) {
        val service = UserApiService()
        if (service.readByUsernameOrEmail(user.username) != null) continue
        service.createUser(
            info = SignUpRequest(
                username = user.username,
                password = user.password,
                email = user.email,
                name = user.name
            ),
            userRoles = user.roles
        )
    }
}

@Serializable
data class InitialUser(
    val username: String,
    val password: String,
    val email: String? = null,
    val name: String? = null,
    val roles: List<String>
)