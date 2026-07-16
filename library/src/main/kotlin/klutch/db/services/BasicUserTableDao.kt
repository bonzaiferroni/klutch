package klutch.db.services

import kampfire.api.Username
import kampfire.api.toEmail
import kampfire.model.EditUserRequest
import kampfire.model.PrivateInfo
import kampfire.model.UserSeed
import klutch.db.DbService
import klutch.db.readFirstOrNull
import klutch.utils.eq
import klutch.utils.eqIgnoreCase
import klutch.utils.serverLog
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

// class BasicUserTableDao: AuthDao<BasicUser, BasicUserId>, DbService() {
//     private fun readByUsername(username: String): BasicUser? =
//         UserAspect.readFirst { BasicUserTable.username.lowerCase() eq username.lowercase() }
//
//     override suspend fun readByUsernameOrEmail(identity: String): BasicUser? = dbQuery {
//         UserAspect.readFirst {
//             eqIdentity(identity)
//         }
//     }
//
//     override suspend fun readPrivateInfo(identity: String) = dbQuery {
//         BasicUserTable.select(BasicUserTable.name, BasicUserTable.email)
//             .where { eqIdentity(identity) }
//             .firstOrNull()
//             ?.let { PrivateInfo(it[BasicUserTable.name], it[BasicUserTable.email]?.toEmail()) }
//             ?: throw IllegalArgumentException("User not found")
//     }
//
//     override suspend fun readIdByUsername(username: Username) = dbQuery {
//         BasicUserTable.select(BasicUserTable.id)
//             .where { BasicUserTable.username.eq(username) }
//             .firstOrNull()?.getOrNull(BasicUserTable.id)?.value?.let { BasicUserId(it)}
//     }
//
//     suspend fun readUserInfo(identity: String): BasicUserInfo {
//         val user = readByUsernameOrEmail(identity) ?: throw IllegalArgumentException("User not found")
//         return BasicUserInfo(
//             username = user.username,
//             roles = user.roles,
//             avatarUrl = user.avatarUrl,
//             createdAt = user.createdAt,
//         )
//     }
//
//     override suspend fun createUser(seed: UserSeed) = dbQuery {
//         val now = Clock.System.now()
//         val user = BasicUser(
//             userId = BasicUserId.random(),
//             name = null,
//             username = seed.request.username,
//             hashedPassword = seed.hashedPassword,
//             email = seed.request.email,
//             roles = seed.roles.toSet(),
//             avatarUrl = null,
//             createdAt = now,
//             updatedAt = now,
//         )
//
//         BasicUserTable.insertAndGetId {
//             it.createRecord(user)
//         }.let { BasicUserId(it.value) }
//     }
//
//     suspend fun updateUser(username: String, info: EditUserRequest) = dbQuery {
//         if (info.deleteUser) {
//             serverLog.logInfo("UserService: Deleting user $username")
//             BasicUserTable.deleteWhere { BasicUserTable.username.eqIgnoreCase(username) }
//         } else {
//             serverLog.logInfo("UserService: Updating user $username")
//             BasicUserTable.update({ BasicUserTable.username.eqIgnoreCase(username) }) {
//                 it[name] = info.name
//                 if (info.deleteName) it[name] = null
//                 it[email] = info.email
//                 if (info.deleteEmail) it[email] = null
//                 it[avatarUrl] = info.avatarUrl
//                 it[updatedAt] = Clock.System.now()
//             }
//         }
//     }
//
//     suspend fun updateUser(user: BasicUserInfo, userId: BasicUserId) = dbQuery {
//         BasicUserTable.update({ BasicUserTable.id.eq(userId)}) {
//             it[this.username] = user.username.value
//             it[this.avatarUrl] = user.avatarUrl
//         } == 1
//     }
//
//     suspend fun checkUsername(username: String) = dbQuery {
//         BasicUserTable.readFirstOrNull { BasicUserTable.username.eqIgnoreCase(username) } == null
//     }
// }
//
// private fun eqIdentity(identity: String) =
//     (BasicUserTable.username.lowerCase() eq identity.lowercase()) or
//         (BasicUserTable.email.lowerCase() eq identity.lowercase())