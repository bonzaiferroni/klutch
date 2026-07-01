package klutch.db.model

import kampfire.api.TableId
import kampfire.model.Token
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SessionRecord(
    val sessionId: SessionId,
    val userId: TableId<Uuid>,
    val token: Token,
    val createdAt: Instant,
    val expiresAt: Instant
)

@JvmInline
value class SessionId(val value: Uuid)
