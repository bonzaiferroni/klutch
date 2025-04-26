package klutch.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A column type for storing vectors in PostgreSQL.
 *
 * @param name The name of the column.
 * @param size The size of the vector.
 */
fun Table.vector(name: String, size: Int = 384): Column<FloatArray> = registerColumn(name, PgVectorColumnType(size))

/**
 * Calculates the cosine distance between this and some [other] column.
 */
infix fun Column<FloatArray>.cosineDistance(other: Column<FloatArray>): Op<Float> {
	return VectorColumnOp(this, other, "<=>")
}

/**
 * Calculates the cosine distance between this and some [other] vector.
 */
infix fun Column<FloatArray>.cosineDistance(other: FloatArray): Op<Float> {
	return VectorValueOp(this, other, "<=>")
}

infix fun Column<FloatArray>.cosineDistanceExp(other: FloatArray): Expression<Float> {
	return VectorValueOp(this, other, "<=>")
}

/**
 * Calculates the cosine distance between this and some [other] column.
 */
infix fun Column<FloatArray?>.cosineDistanceNullable(other: Column<FloatArray?>): Op<Float> {
	return VectorNullableColumnOp(this, other, "<=>")
}

/**
 * Calculates the cosine distance between this and some [other] vector.
 */
infix fun Column<FloatArray?>.cosineDistanceNullable(other: FloatArray): Op<Float> {
	return VectorNullableValueOp(this, other, "<=>")
}

/**
 * Calculates the L2 distance between this and some [other] column.
 */
infix fun Column<FloatArray>.l2Distance(other: Column<FloatArray>): Op<Float> {
	return VectorColumnOp(this, other, "<->")
}

/**
 * Calculates the L2 distance between this and some [other] vector.
 */
infix fun Column<FloatArray>.l2Distance(other: FloatArray): Op<Float> {
	return VectorValueOp(this, other, "<->")
}

/**
 * Calculates the inner product between this and some [other] column.
 */
infix fun Column<FloatArray>.innerProduct(other: Column<FloatArray>): Op<Float> {
	return VectorColumnOp(this, other, "<#>")
}

/**
 * Calculates the inner product between this and some [other] vector.
 */
infix fun Column<FloatArray>.innerProduct(other: FloatArray): Op<Float> {
	return VectorValueOp(this, other, "<#>")
}

/**
 * Calculates the L1 distance between this and some [other] column.
 */
infix fun Column<FloatArray>.l1Distance(other: Column<FloatArray>): Op<Float> {
	return VectorColumnOp(this, other, "<+>")
}

/**
 * Calculates the L1 distance between this and some [other] vector.
 */
infix fun Column<FloatArray>.l1Distance(other: FloatArray): Op<Float> {
	return VectorValueOp(this, other, "<+>")
}

/**
 * Initializes the [Database] to use the pgvector extension and registers managers for the correct type.
 */
fun Database.usePGVector() {
	TransactionManager.registerManager(this, PgVectorManager(TransactionManager.manager))
	transaction(this) {
		exec("CREATE EXTENSION IF NOT EXISTS vector;")
	}
}