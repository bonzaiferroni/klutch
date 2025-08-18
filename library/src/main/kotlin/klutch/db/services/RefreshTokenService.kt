package klutch.db.services

import kotlinx.datetime.Clock
import klutch.db.DbService
import klutch.db.model.RefreshToken
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.toSessionToken
import kabinet.utils.epochSecondsNow
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class RefreshTokenService : DbService() {
    suspend fun readToken(value: String): RefreshToken? = dbQuery {
        RefreshTokenTable.select(RefreshTokenTable.columns)
            .where { RefreshTokenTable.token eq value }
            .firstOrNull()?.toSessionToken()
    }

    suspend fun createToken(userId: String, generatedToken: String, stayLoggedIn: Boolean) = dbQuery {
        val requestedTTL = when(stayLoggedIn) {
            true -> REFRESH_TOKEN_LONG_TTL
            false -> REFRESH_TOKEN_TEMP_TTL
        }
        RefreshTokenTable.insert {
            it[user] = userId.toUUID()
            it[token] = generatedToken
            it[createdAt] = Clock.epochSecondsNow()
            it[ttl] = requestedTTL

            it[issuer] = "http://localhost:8080/"
        }
    }

    suspend fun deleteToken(value: String) = dbQuery {
        RefreshTokenTable.deleteWhere { token eq value }
    }
}

const val REFRESH_TOKEN_LONG_TTL = 60 * 60 * 24 * 30 // 30 days
const val REFRESH_TOKEN_TEMP_TTL = 60 * 2 // 2 hours