package klutch.db.services

import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.model.RefreshToken
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.toSessionToken
import kabinet.utils.epochSecondsNow
import kampfire.api.TableId
import klutch.environment.readEnvFromPath
import klutch.utils.toUUID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock

class RefreshTokenService(
    private val table: RefreshTokenTable
) : DbService() {
    private val env = readEnvFromPath()

    suspend fun readToken(value: String): RefreshToken? = dbQuery {
        table.select(table.columns)
            .where { table.token eq value }
            .firstOrNull()?.toSessionToken(table)
    }

    suspend fun createToken(userId: TableId<String>, generatedToken: String, stayLoggedIn: Boolean) = dbQuery {
        val requestedTTL = when(stayLoggedIn) {
            true -> REFRESH_TOKEN_LONG_TTL
            false -> REFRESH_TOKEN_TEMP_TTL
        }
        table.insert {
            it[user] = userId.toUUID()
            it[token] = generatedToken
            it[createdAt] = Clock.epochSecondsNow()
            it[ttl] = requestedTTL

            it[issuer] = env.read("HOST_ADDRESS")
        }
    }

    suspend fun deleteToken(value: String) = dbQuery {
        table.deleteWhere { token eq value }
    }
}

const val REFRESH_TOKEN_LONG_TTL = 60 * 60 * 24 * 30 // 30 days
const val REFRESH_TOKEN_TEMP_TTL = 60 * 2 // 2 hours