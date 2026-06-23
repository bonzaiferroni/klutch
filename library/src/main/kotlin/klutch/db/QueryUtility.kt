package klutch.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

fun Query.printQuery(): Query = apply {
    val transaction = TransactionManager.current()
    val prepared = prepareSQL(transaction)
    println(prepared)
}

fun <T> Query.mapFirstOrNull(transform: (ResultRow) -> T?) = firstOrNull()?.let { transform(it) }

fun <T> Query.mapFirst(transform: (ResultRow) -> T) = transform(first())

fun <T1, T2> Query.mapFirst(column: Column<T1>, transform: (T1) -> T2) = transform(first()[column])

fun Query.explainAnalyze(): Query {
    val sql = prepareSQL(QueryBuilder(false))
    TransactionManager.current().exec(
        "EXPLAIN ANALYZE $sql",
        explicitStatementType = StatementType.SELECT
    ) { rs ->
        while (rs.next()) {
            println(rs.getString(1))
        }
    }
    return this
}