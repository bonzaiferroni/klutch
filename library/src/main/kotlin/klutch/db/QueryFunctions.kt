package klutch.db

import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

fun Query.printQuery(): Query = apply {
    val transaction = TransactionManager.current()
    val prepared = prepareSQL(transaction)
    println(prepared)
}