package klutch.db.services

import kampfire.api.TableId
import kampfire.api.Username
import kampfire.model.AuthUser
import kampfire.model.PrivateInfo
import kampfire.model.UserSeed
import java.util.UUID
import kotlin.uuid.Uuid

interface AuthDao<User: AuthUser, Id: AuthId> {

    suspend fun createUser(seed: UserSeed): AuthId
    suspend fun readIdByUsername(username: Username): Id?
    suspend fun readByUsernameOrEmail(identity: String): User?
    suspend fun readPrivateInfo(identity: String): PrivateInfo?
    // suspend fun readUserInfo(identity: String): BasicUserInfo
    // suspend fun updateUser(user: BasicUserInfo, userId: UserId): Boolean
}

typealias AuthId = TableId<Uuid>