package klutch.db.tables

import kampfire.model.UserId
import kampfire.model.UserRole
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.model.User
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UserTable : UUIDTable("user") {
    val name = text("name").nullable()
    val username = text("username")
    val hashedPassword = text("hashed_password")
    val salt = text("salt")
    val email = text("email").nullable()
    val roles = array<String>("roles")
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

// Row mapper
fun ResultRow.toUser() = User(
    userId = UserId(this[UserTable.id].value.toStringId()),
    name = this[UserTable.name],
    username = this[UserTable.username],
    hashedPassword = this[UserTable.hashedPassword],
    salt = this[UserTable.salt],
    email = this[UserTable.email],
    roles = this[UserTable.roles].map { UserRole.valueOf(it) }.toSet(),
    avatarUrl = this[UserTable.avatarUrl],
    createdAt = this[UserTable.createdAt].toInstantFromUtc(),
    updatedAt = this[UserTable.updatedAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(user: User) {
    this[UserTable.id] = user.userId.value.toUUID()
    writeUpdate(user)
}

fun UpdateBuilder<*>.writeUpdate(user: User) {
    this[UserTable.name] = user.name
    this[UserTable.username] = user.username
    this[UserTable.hashedPassword] = user.hashedPassword
    this[UserTable.salt] = user.salt
    this[UserTable.email] = user.email
    this[UserTable.roles] = user.roles.map { it.name }
    this[UserTable.avatarUrl] = user.avatarUrl
    this[UserTable.createdAt] = user.createdAt.toLocalDateTimeUtc()
    this[UserTable.updatedAt] = user.updatedAt.toLocalDateTimeUtc()
}

