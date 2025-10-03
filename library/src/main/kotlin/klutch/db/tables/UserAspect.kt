package klutch.db.tables

import kabinet.model.UserId
import klutch.db.Aspect
import kabinet.model.UserRole
import kabinet.utils.toInstantFromUtc
import klutch.db.model.User
import klutch.utils.toStringId
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
