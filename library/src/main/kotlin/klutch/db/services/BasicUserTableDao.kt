package klutch.db.services

import kampfire.model.BasicUser
import kampfire.model.BasicUserInfo
import kampfire.model.EditUserRequest
import kampfire.model.PrivateInfo
import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.readFirstOrNull
import klutch.db.tables.BasicUserTable
import klutch.db.tables.UserAspect
import klutch.db.tables.writeFull
import klutch.utils.eq
import klutch.utils.eqLowercase
import klutch.utils.serverLog
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

class BasicUserTableDao: AuthUserDao<BasicUser>, DbService() {
    private fun readByUsername(username: String): BasicUser? =
        UserAspect.readFirst { BasicUserTable.username.lowerCase() eq username.lowercase() }

    override suspend fun readByUsernameOrEmail(identity: String): BasicUser? = dbQuery {
        UserAspect.readFirst {
            eqIdentity(identity)
        }
    }

    override suspend fun readPrivateInfo(identity: String) = dbQuery {
        BasicUserTable.select(BasicUserTable.name, BasicUserTable.email)
            .where { eqIdentity(identity) }
            .firstOrNull()
            ?.let { PrivateInfo(it[BasicUserTable.name], it[BasicUserTable.email]) }
            ?: throw IllegalArgumentException("User not found")
    }

    suspend fun readIdByUsername(username: String) = dbQuery {
        BasicUserTable.select(BasicUserTable.id)
            .where { BasicUserTable.username.eq(username) }
            .firstOrNull()?.getOrNull(BasicUserTable.id)?.value
    }

    override suspend fun readUserInfo(identity: String): BasicUserInfo {
        val user = readByUsernameOrEmail(identity) ?: throw IllegalArgumentException("User not found")
        return BasicUserInfo(
            username = user.username,
            roles = user.roles,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
        )
    }

    override suspend fun createUser(user: BasicUser) = dbQuery {
        BasicUserTable.insertAndGetId {
            it.writeFull(user)
        }.value
    }

    suspend fun updateUser(username: String, info: EditUserRequest) = dbQuery {
        if (info.deleteUser) {
            serverLog.logInfo("UserService: Deleting user $username")
            BasicUserTable.deleteWhere { BasicUserTable.username.eqLowercase(username) }
        } else {
            serverLog.logInfo("UserService: Updating user $username")
            BasicUserTable.update({ BasicUserTable.username.eqLowercase(username) }) {
                it[name] = info.name
                if (info.deleteName) it[name] = null
                it[email] = info.email
                if (info.deleteEmail) it[email] = null
                it[avatarUrl] = info.avatarUrl
                it[updatedAt] = Clock.System.now()
            }
        }
    }

    override suspend fun updateUser(user: BasicUserInfo, userId: UserId) = dbQuery {
        BasicUserTable.update({ BasicUserTable.id.eq(userId)}) {
            it[this.username] = user.username
            it[this.avatarUrl] = user.avatarUrl
        } == 1
    }

    suspend fun checkUsername(username: String) = dbQuery {
        BasicUserTable.readFirstOrNull { BasicUserTable.username.eqLowercase(username) } == null
    }
}

private fun eqIdentity(identity: String) =
    (BasicUserTable.username.lowerCase() eq identity.lowercase()) or
        (BasicUserTable.email.lowerCase() eq identity.lowercase())