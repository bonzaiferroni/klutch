package klutch.db.tables

import klutch.db.Aspect
import klutch.db.model.User
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
