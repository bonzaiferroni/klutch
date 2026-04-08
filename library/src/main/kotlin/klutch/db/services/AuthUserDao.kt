package klutch.db.services

import kampfire.model.AuthUser
import kampfire.model.BasicUserInfo
import kampfire.model.PrivateInfo
import kampfire.model.SignUpRequest
import kampfire.model.UserId
import java.util.UUID

interface AuthUserDao<T: AuthUser> {

    suspend fun createUser(user: T): UUID
    suspend fun readByUsernameOrEmail(identity: String): T?
    suspend fun readPrivateInfo(identity: String): PrivateInfo?
    suspend fun readUserInfo(identity: String): BasicUserInfo
    suspend fun updateUser(user: BasicUserInfo, userId: UserId): Boolean
}