package klutch.db.services

import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.db.readById
import klutch.db.readFirstOrNull
import klutch.db.tables.UserTable
import klutch.db.tables.toUser
import klutch.db.tables.toUserDto
import klutch.utils.eqLowercase
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.v1.jdbc.select

class UserTableDao: DbService() {
    suspend fun readById(userId: UserId) = dbQuery {
        UserTable.readById(userId.toUUID()).toUser()
    }

    suspend fun readIdByUsername(username: String) = dbQuery {
        UserTable.select(UserTable.id).where { UserTable.username.eqLowercase(username) }.firstOrNull()?.let {
            UserId(it[UserTable.id].value.toStringId())
        }
    }
}