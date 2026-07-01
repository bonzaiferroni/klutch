package klutch.db.services

import kampfire.api.TableUuid
import kampfire.api.Username
import kampfire.model.HashedToken
import kampfire.model.PrivateInfo
import kampfire.model.SessionPrincipal
import kampfire.model.UserRecord
import kampfire.model.UserSeed
import kotlin.time.Duration

interface SessionService {
    suspend fun createSessionRecord(userId: TableUuid, token: HashedToken, isTemp: Boolean, ttl: Duration): Boolean
    suspend fun deleteSession(token: HashedToken): Int
    suspend fun deleteSessions(userId: TableUuid): Int

    suspend fun createUserRecord(seed: UserSeed): TableUuid
    suspend fun readIdByUsername(username: Username): TableUuid?
    suspend fun readByUsernameOrEmail(identity: String): UserRecord?
    suspend fun readPrivateInfo(username: Username): PrivateInfo?
    suspend fun readSaltExists(salt: String): Boolean

    suspend fun generateUsername(): Username
    suspend fun readSessionPrincipal(token: HashedToken): SessionPrincipal?
}
