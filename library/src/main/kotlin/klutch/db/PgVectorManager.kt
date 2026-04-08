package klutch.db

import com.pgvector.PGvector
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.JdbcTransactionManager
import java.sql.Connection

class PgVectorManager(private val impl: JdbcTransactionManager) : JdbcTransactionManager by impl {

	override fun newTransaction(isolation: Int, readOnly: Boolean, outerTransaction: JdbcTransaction?): JdbcTransaction {
		val transaction = impl.newTransaction(isolation, readOnly, outerTransaction)
		val conn = transaction.connection.connection as Connection
		PGvector.addVectorType(conn)
		return transaction
	}
}