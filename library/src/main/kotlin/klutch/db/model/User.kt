package klutch.db.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kabinet.model.UserRole
import kabinet.model.PrivateInfo

@Serializable
data class User(
    val id: Long = 0,
    val name: String? = "",
    val username: String = "",
    val hashedPassword: String = "",
    val salt: String = "",
    val email: String? = null,
    val roles: Set<UserRole> = emptySet(),
    val avatarUrl: String? = null,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
)

fun User.toPrivateInfo() = PrivateInfo(
    name = this.name,
    email = this.email,
)