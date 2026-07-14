package klutch.db.tables

import kampfire.api.HashedPassword
import kampfire.api.toEmail
import kampfire.api.toUsername
import kampfire.model.BasicUser
import kampfire.model.BasicUserId
import kampfire.model.UserRole
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.datetime.timestamp

object BasicUserTable : UuidTable("user") {
    val name = text("name").nullable()
    val username = text("username")
    val hashedPassword = text("hashed_password")
    val salt = text("salt")
    val email = text("email").nullable()
    val roles = array<String>("roles")
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

// Row mapper
fun ResultRow.toUser() = BasicUser(
    userId = BasicUserId(this[BasicUserTable.id].value),
    name = this[BasicUserTable.name],
    username = this[BasicUserTable.username].toUsername(),
    hashedPassword = HashedPassword(this[BasicUserTable.hashedPassword]),
    salt = this[BasicUserTable.salt],
    email = this[BasicUserTable.email]?.toEmail(),
    roles = this[BasicUserTable.roles].map { UserRole.valueOf(it) }.toSet(),
    avatarUrl = this[BasicUserTable.avatarUrl],
    createdAt = this[BasicUserTable.createdAt],
    updatedAt = this[BasicUserTable.updatedAt],
)

// Updaters
fun UpdateBuilder<*>.createRecord(user: BasicUser) {
    this[BasicUserTable.id] = user.userId.value
    updateRecord(user)
}

fun UpdateBuilder<*>.updateRecord(user: BasicUser) {
    this[BasicUserTable.name] = user.name
    this[BasicUserTable.username] = user.username.value
    this[BasicUserTable.hashedPassword] = user.hashedPassword.value
    this[BasicUserTable.salt] = user.salt
    this[BasicUserTable.email] = user.email?.value
    this[BasicUserTable.roles] = user.roles.map { it.name }
    this[BasicUserTable.avatarUrl] = user.avatarUrl
    this[BasicUserTable.createdAt] = user.createdAt
    this[BasicUserTable.updatedAt] = user.updatedAt
}
