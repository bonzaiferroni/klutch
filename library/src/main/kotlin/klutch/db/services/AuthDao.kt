package klutch.db.services

import kampfire.api.TableId
import kampfire.model.AuthUser
import kampfire.model.PrivateInfo
import kampfire.model.UserSeed
import java.util.UUID

interface AuthDao<User: AuthUser, Id: AuthId> {

    suspend fun createUser(seed: UserSeed): UUID
    suspend fun readIdByUsername(username: String): Id?
    suspend fun readByUsernameOrEmail(identity: String): User?
    suspend fun readPrivateInfo(identity: String): PrivateInfo?
    suspend fun readSaltExists(salt: String): Boolean
    // suspend fun readUserInfo(identity: String): BasicUserInfo
    // suspend fun updateUser(user: BasicUserInfo, userId: UserId): Boolean
}

typealias AuthId = TableId<String>