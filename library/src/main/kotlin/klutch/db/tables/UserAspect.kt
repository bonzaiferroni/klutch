package klutch.db.tables

import kampfire.model.BasicUser
import klutch.db.Aspect
import org.jetbrains.exposed.v1.core.ResultRow

object UserAspect: Aspect<UserAspect, BasicUser>(
    BasicUserTable,
    ResultRow::toUser
) {
    val id = add(BasicUserTable.id)
    val name = add(BasicUserTable.name)
    val username = add(BasicUserTable.username)
    val hashedPassword = add(BasicUserTable.hashedPassword)
    val salt = add(BasicUserTable.salt)
    val email = add(BasicUserTable.email)
    val roles = add(BasicUserTable.roles)
    val avatarUrl = add(BasicUserTable.avatarUrl)
    val createdAt = add(BasicUserTable.createdAt)
    val updatedAt = add(BasicUserTable.updatedAt)
}
