package klutch.db.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
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