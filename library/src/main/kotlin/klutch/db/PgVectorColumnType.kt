package klutch.db

import com.pgvector.PGvector
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.sql.ResultSet

// referenced:
// https://github.com/Martmists-GH/mlutils/blob/master/src/main/kotlin/com/martmists/mlutils/compat/exposed/PgVectorColumnType.kt

class PgVectorColumnType(private val size: Int) : ColumnType<FloatArray>() {

    override fun sqlType(): String = "vector($size)"

    // Read as text to dodge PGobject/PGvector classloader woes
    override fun readObject(rs: ResultSet, index: Int): FloatArray? {
        val s = rs.getString(index) ?: return null
        return parseVector(s)
    }

    override fun valueFromDB(value: Any): FloatArray = when (value) {
        is FloatArray -> {
            require(value.size == size) { "Embedding size must be $size" }
            value
        }
        is String -> parseVector(value)
        is PGobject -> {
            val v = value.value ?: error("Null vector text in PGobject")
            parseVector(v)
        }
        else -> error("Unexpected DB value for vector: ${value::class.java.name}")
    }

    override fun notNullValueToDB(value: FloatArray): Any {
        require(value.size == size) { "Embedding size must be $size" }
        return PGobject().apply {
            setType("vector")
            setValue(value.joinToString(prefix = "[", postfix = "]"))
        }
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        if (value == null) {
            stmt.setNull(index, this)
            return
        }
        val vec = valueFromDB(value)
        val pg = PGobject().apply {
            setType("vector")
            setValue(vec.joinToString(prefix = "[", postfix = "]"))
        }
        stmt[index] = pg
    }

    private fun parseVector(text: String): FloatArray {
        val inner = text.trim().removePrefix("[").removeSuffix("]")
        if (inner.isBlank()) return FloatArray(0)
        return inner.split(',').map { it.trim().toFloat() }.toFloatArray()
    }
}