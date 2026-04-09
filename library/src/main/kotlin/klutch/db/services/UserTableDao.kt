package klutch.db.services

import kampfire.model.BasicUserId
import klutch.db.DbService
import klutch.db.readById
import klutch.db.tables.BasicUserTable
import klutch.db.tables.toUser
import klutch.utils.eqLowercase
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.v1.jdbc.select

class UserTableDao: DbService() {
    suspend fun readById(userId: BasicUserId) = dbQuery {
        BasicUserTable.readById(userId.toUUID()).toUser()
    }

    suspend fun readIdByUsername(username: String) = dbQuery {
        BasicUserTable.select(BasicUserTable.id).where { BasicUserTable.username.eqLowercase(username) }.firstOrNull()?.let {
            BasicUserId(it[BasicUserTable.id].value.toStringId())
        }
    }
}