package klutch.db.model

import kotlinx.serialization.Serializable
import kampfire.model.UserRole
import kampfire.model.PrivateInfo
import kampfire.model.UserId
import kotlin.time.Instant

@Serializable
data class User(
    val userId: UserId,
    val name: String?,
    val username: String,
    val hashedPassword: String,
    val salt: String,
    val email: String?,
    val roles: Set<UserRole>,
    val avatarUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun User.toPrivateInfo() = PrivateInfo(
    name = this.name,
    email = this.email,
)