package klutch.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

abstract class DbService(
    val defaultMaxAttempts: Int = 1,
    val defaultAddLogger: Boolean = false,
) {
    suspend fun <T> dbQuery(
        addLogger: Boolean = defaultAddLogger,
        maxAttempts: Int = defaultMaxAttempts,
        block: suspend Transaction.() -> T
    ): T =
        suspendTransaction {
            if (addLogger) {
                addLogger(StdOutSqlLogger)
            }
            this.maxAttempts = maxAttempts
            block()
        }
}