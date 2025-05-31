package klutch.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class DbService(
    val defaultMaxAttepts: Int = 1
) {
    suspend fun <T> dbQuery(maxAttempts: Int = defaultMaxAttepts, block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            this.maxAttempts = maxAttempts
            block()
        }
}