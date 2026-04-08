package klutch.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

abstract class DbService(
    val defaultMaxAttempts: Int = 1
) {
    suspend fun <T> dbQuery(maxAttempts: Int = defaultMaxAttempts, block: suspend Transaction.() -> T): T =
        suspendTransaction {
            this.maxAttempts = maxAttempts
            block()
        }
}