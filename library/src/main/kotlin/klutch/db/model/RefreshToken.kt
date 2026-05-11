package klutch.db.model

import kabinet.utils.epochSecondsNow
import kampfire.api.TableId
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class RefreshToken(
    val id: Long,
    val userId: TableId<String>,
    val token: String,
    val createdAt: Instant,
    val expiresAt: Instant,
) {
    val isExpired get() = Clock.System.now() > expiresAt
    val needsRotating get() = Clock.System.now() > expiresAt - 7.days
}

