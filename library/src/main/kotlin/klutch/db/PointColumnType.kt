package klutch.db

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.geometric.PGpoint

object PointColumnType : ColumnType<PGpoint>() {
    override fun sqlType(): String = "POINT"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        stmt[index] = when (value) {
            is Pair<*, *> -> PGpoint((value.first as Number).toDouble(), (value.second as Number).toDouble())
            is PGpoint -> value
            else -> error("Unsupported value type for POINT: $value")
        }
    }

    override fun valueFromDB(value: Any): PGpoint = when (value) {
        is PGpoint -> value
        is String -> PGpoint(value)
        else -> error("Unexpected value from DB: $value")
    }
}

fun Table.point(name: String) = registerColumn(name, PointColumnType)