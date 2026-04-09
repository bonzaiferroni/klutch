package klutch.db.tables

import klutch.db.model.RefreshToken
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

class RefreshTokenTable(
    userTable: UUIDTable
) : LongIdTable("refresh_token") {
    val user = reference("user_id", userTable, onDelete = ReferenceOption.CASCADE)
    val token = text("token")
    val createdAt = long("created_at") // epoch seconds
    val ttl = integer("ttl") // in seconds
    val issuer = text("issuer")
}

//fun SessionTokenEntity.fromModel(data: SessionToken) {
//    user = UserRow[data.userId]
//    token = data.token
//    createdAt = data.createdAt
//    expiresAt = data.expiresAt
//    issuer = data.issuer
//}

fun ResultRow.toSessionToken(table: RefreshTokenTable) = RefreshToken(
    id = this[table.id].value,
    userId = this[table.user].value,
    token = this[table.token],
    createdAt = this[table.createdAt],
    ttl = this[table.ttl],
    issuer = this[table.issuer],
)