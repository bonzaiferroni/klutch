package klutch.db

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Query.printQuery(): Query = apply {
    val transaction = TransactionManager.current()
    val prepared = prepareSQL(transaction)
    println(prepared)
}