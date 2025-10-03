package klutch.db

import kabinet.utils.Environment
import klutch.utils.dbLog
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

fun initDb(env: Environment, tables: List<Table>, execution: (Transaction.() -> Unit)? = null) {
    dbLog.logInfo("initializing db")
    val db = connectDb(env)

    transaction(db) {
        SchemaUtils.create(*tables.toTypedArray())
        execution?.invoke(this)
    }


}

fun connectDb(env: Environment) = Database.connect(
    url = env.read(DB_URL_KEY),
    driver = "org.postgresql.Driver",
    user = env.read(DB_USER_KEY),
    password = env.read(DB_PASSWORD_KEY)
)

const val DB_USER_KEY = "DB_USER"
const val DB_URL_KEY = "DB_URL"
const val DB_PASSWORD_KEY = "DB_PW"