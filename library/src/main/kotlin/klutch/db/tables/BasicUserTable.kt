package klutch.db.tables

import kampfire.model.BasicUser
import kampfire.model.UserId
import kampfire.model.UserRole
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.datetime.timestamp

object BasicUserTable : UUIDTable("user") {
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
    userId = UserId(this[BasicUserTable.id].value.toStringId()),
    name = this[BasicUserTable.name],
    username = this[BasicUserTable.username],
    hashedPassword = this[BasicUserTable.hashedPassword],
    salt = this[BasicUserTable.salt],
    email = this[BasicUserTable.email],
    roles = this[BasicUserTable.roles].map { UserRole.valueOf(it) }.toSet(),
    avatarUrl = this[BasicUserTable.avatarUrl],
    createdAt = this[BasicUserTable.createdAt],
    updatedAt = this[BasicUserTable.updatedAt],
)

// Updaters
fun UpdateBuilder<*>.writeFull(user: BasicUser) {
    this[BasicUserTable.id] = user.userId.value.toUUID()
    writeUpdate(user)
}

fun UpdateBuilder<*>.writeUpdate(user: BasicUser) {
    this[BasicUserTable.name] = user.name
    this[BasicUserTable.username] = user.username
    this[BasicUserTable.hashedPassword] = user.hashedPassword
    this[BasicUserTable.salt] = user.salt
    this[BasicUserTable.email] = user.email
    this[BasicUserTable.roles] = user.roles.map { it.name }
    this[BasicUserTable.avatarUrl] = user.avatarUrl
    this[BasicUserTable.createdAt] = user.createdAt
    this[BasicUserTable.updatedAt] = user.updatedAt
}

