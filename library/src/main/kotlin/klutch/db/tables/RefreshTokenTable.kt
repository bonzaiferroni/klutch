package klutch.db.tables

import kampfire.api.TableId
import klutch.db.model.RefreshToken
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.Uuid

class RefreshTokenTable(
    userTable: UuidTable,
    val tableIdOf: (Uuid) -> TableId<Uuid>
) : LongIdTable("refresh_token") {
    val user = reference("user_id", userTable, onDelete = ReferenceOption.CASCADE)
    val token = text("token")
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
}

fun ResultRow.toSessionToken(table: RefreshTokenTable) = RefreshToken(
    id = this[table.id].value,
    userId = table.tableIdOf(this[table.user].value),
    token = this[table.token],
    createdAt = this[table.createdAt],
    expiresAt = this[table.expiresAt],
)