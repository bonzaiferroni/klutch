package klutch.db.services

import klutch.db.DbService
import klutch.db.model.RefreshToken
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.toSessionToken
import kampfire.api.TableId
import kampfire.model.TokenInfo
import klutch.utils.eq
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

// class RefreshTokenService(
//     private val table: RefreshTokenTable,
// ) : DbService() {
//
//     suspend fun readToken(value: String): RefreshToken? = dbQuery {
//         table.select(table.columns)
//             .where { table.token eq value }
//             .firstOrNull()?.toSessionToken(table)
//     }
//
//     // suspend fun createToken(userId: TableId<Uuid>, generatedToken: String, stayLoggedIn: Boolean) = dbQuery {
//     //     val requestedTTL = when(stayLoggedIn) {
//     //         true -> REFRESH_TOKEN_LONG_TTL
//     //         false -> REFRESH_TOKEN_TEMP_TTL
//     //     }
//     //     table.insert {
//     //         it[user] = userId.value
//     //         it[token] = generatedToken
//     //         it[createdAt] = Clock.System.now()
//     //         it[expiresAt] = Clock.System.now() + requestedTTL
//     //     }
//     //     TokenInfo(generatedToken, requestedTTL.inWholeSeconds.toInt())
//     // }
//
//     suspend fun deleteTokens(userId: TableId<Uuid>) = dbQuery {
//         table.deleteWhere { table.user.eq(userId) }
//     }
//
//     suspend fun deleteToken(value: String) = dbQuery {
//         table.deleteWhere { token eq value }
//     }
// }
//
// val REFRESH_TOKEN_LONG_TTL = 30.days
// val REFRESH_TOKEN_TEMP_TTL = 1.days