package klutch.db

import com.pgvector.PGvector
import org.jetbrains.exposed.sql.ColumnType
import java.sql.ResultSet

// referenced:
// https://github.com/Martmists-GH/mlutils/blob/master/src/main/kotlin/com/martmists/mlutils/compat/exposed/PgVectorColumnType.kt

class PgVectorColumnType(private val size: Int) : ColumnType<FloatArray>() {
	override fun sqlType(): String = "vector($size)"

	override fun readObject(rs: ResultSet, index: Int) = rs.getObject(index) as PGvector?

	override fun validateValueBeforeUpdate(value: FloatArray?) {
		if (value !is FloatArray) error("Value must be an Embedding")
		require(value.size == size) { "Embedding size must be $size" }
	}

	override fun valueFromDB(value: Any): FloatArray = when (value) {
		is PGvector -> value.toArray()
		else -> error("Unexpected value: $value")
	}

	override fun notNullValueToDB(value: FloatArray) = PGvector(value)
}