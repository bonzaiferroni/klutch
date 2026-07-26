package klutch.db.services

import kampfire.api.Email
import kampfire.api.PasswordHash
import kampfire.api.LoginIdentity
import kampfire.api.TableUuid
import kampfire.api.Username
import kampfire.model.CallerId
import kampfire.model.HashedToken
import kampfire.model.PrivateInfo
import kampfire.model.Session
import kampfire.model.SessionIdentity
import kampfire.model.Token
import kampfire.model.UserRecord
import kampfire.model.UserSeed
import klutch.db.model.SessionId
import kotlin.time.Duration
import kotlin.time.Instant

interface SessionService {
    suspend fun createSession(userId: TableUuid, token: HashedToken, ttl: Duration, expiresAt: Instant): Boolean
    suspend fun deleteSession(token: Token): Int
    suspend fun deleteSessions(userId: TableUuid, sparedSessionId: SessionId?): Int

    suspend fun createUserRecord(seed: UserSeed): TableUuid
    suspend fun upgradeAccount(callerId: CallerId, passwordHash: PasswordHash, email: Email?): Boolean
    suspend fun readIdByUsername(username: Username): TableUuid?
    suspend fun readByUsernameOrEmail(identity: LoginIdentity): UserRecord?
    suspend fun readPrivateInfo(username: Username): PrivateInfo?
    suspend fun checkUsernameExists(username: Username): Boolean
    suspend fun checkGuest(token: Token): Username?

    suspend fun generateUsername(): Username
    suspend fun readSessionIdentity(token: Token): SessionIdentity?
    suspend fun extendSession(session: Session): Session
    suspend fun refreshActivity(callerId: CallerId)
}
