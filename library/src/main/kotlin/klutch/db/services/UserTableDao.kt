package klutch.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.readById
import klutch.db.tables.UserTable
import klutch.db.tables.toUser
import klutch.utils.toUUID

class UserTableDao: DbService() {
    suspend fun readById(userId: UserId) = dbQuery {
        UserTable.readById(userId.toUUID()).toUser()
    }
}