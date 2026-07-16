package klutch.db.services

import klutch.db.DbService
import klutch.db.readById
import klutch.utils.eqIgnoreCase
import org.jetbrains.exposed.v1.jdbc.select

// class UserTableDao: DbService() {
//     suspend fun readById(userId: BasicUserId) = dbQuery {
//         BasicUserTable.readById(userId.value).toUser()
//     }
//
//     suspend fun readIdByUsername(username: String) = dbQuery {
//         BasicUserTable.select(BasicUserTable.id).where { BasicUserTable.username.eqIgnoreCase(username) }.firstOrNull()?.let {
//             BasicUserId(it[BasicUserTable.id].value)
//         }
//     }
// }