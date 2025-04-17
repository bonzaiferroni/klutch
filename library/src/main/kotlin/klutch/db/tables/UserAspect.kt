package klutch.db.tables

import klutch.db.Aspect
import kabinet.model.UserRole
import klutch.db.model.User
import klutch.utils.toInstantUtc
import org.jetbrains.exposed.sql.ResultRow

object UserAspect: Aspect<UserAspect, User>(
    UserTable,
    ResultRow::toUser
) {
    val id = add(UserTable.id)
    val name = add(UserTable.name)
    val username = add(UserTable.username)
    val hashedPassword = add(UserTable.hashedPassword)
    val salt = add(UserTable.salt)
    val email = add(UserTable.email)
    val roles = add(UserTable.roles)
    val avatarUrl = add(UserTable.avatarUrl)
    val createdAt = add(UserTable.createdAt)
    val updatedAt = add(UserTable.updatedAt)
}

fun ResultRow.toUser() = User(
    id = this[UserAspect.id].value,
    name = this[UserAspect.name],
    username = this[UserAspect.username],
    hashedPassword = this[UserAspect.hashedPassword],
    salt = this[UserAspect.salt],
    email = this[UserAspect.email],
    roles = this[UserAspect.roles].map { UserRole.valueOf(it) }.toSet(),
    avatarUrl = this[UserAspect.avatarUrl],
    createdAt = this[UserAspect.createdAt].toInstantUtc(),
    updatedAt = this[UserAspect.updatedAt].toInstantUtc(),
)