package klutch.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.db.model.RefreshToken
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object RefreshTokenTable : LongIdTable("refresh_token") {
    val user = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
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

fun ResultRow.toSessionToken() = RefreshToken(
    id = this[RefreshTokenTable.id].value,
    userId = UserId(this[RefreshTokenTable.user].value.toStringId()),
    token = this[RefreshTokenTable.token],
    createdAt = this[RefreshTokenTable.createdAt],
    ttl = this[RefreshTokenTable.ttl],
    issuer = this[RefreshTokenTable.issuer],
)