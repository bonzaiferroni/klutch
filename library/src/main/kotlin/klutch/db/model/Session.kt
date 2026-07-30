package klutch.db.model

import kampfire.api.TableId
import kampfire.api.Username
import kampfire.model.AccountType
import kampfire.model.Token
import kampfire.model.UserRole
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Session(
    val sessionId: SessionId,
    val token: Token,
    val ttlSeconds: Int,
    val activeAt: Instant,
    val expiresAt: Instant,
) {
    companion object {
        val activityPeriod = 1.hours
    }

    fun activityRefreshDue() = activeAt < Clock.System.now() - activityPeriod
    fun pastHalfLife() = (expiresAt - Clock.System.now()) < ttlSeconds.seconds / 2
}

@JvmInline
value class SessionId(val value: Uuid)

data class Identity(
    val callerId: CallerId,
    val username: Username,
    val roles: Set<UserRole>,
    val accountType: AccountType,
)

@JvmInline
value class CallerId(override val value: Uuid): TableId<Uuid>

data class SessionIdentity(
    val session: Session,
    val identity: Identity,
)