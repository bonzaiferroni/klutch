package klutch.db

import klutch.environment.Environment
import klutch.environment.readEnvFromPath
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun generateMigrationScript(env: Environment, tables: List<Table>) {
	migrate("../test", false, env, tables)
	migrate("", true, env, tables)
}

@OptIn(ExperimentalDatabaseMigrationApi::class)
private fun migrate(
	protocol: String,
	applyMigration: Boolean,
	env: Environment,
	tables: List<Table>,
) {
	val folder = File("$MIGRATIONS_DIRECTORY/$protocol")
	if (!folder.exists()) folder.mkdirs()
	val file = folder.listFiles()?.firstNotNullOfOrNull {
		if (it.name.endsWith(".sql") || it.name.endsWith(".txt")) null
		else if (File("${it.absolutePath}.sql").exists()) null
		else it
	} ?: return

	val name = file.name

	val isBaseline = folder.listFiles()?.count { it.isFile && it.name.endsWith(".sql") } == 0
	val db = Database.connect(
		url = env.read("DB_URL"),
		driver = "org.postgresql.Driver",
		user = env.read("DB_USER"),
		password = env.read("DB_PW")
	)
	TransactionManager.registerManager(db, PgVectorManager(TransactionManager.manager))

	if (file.readText().isNotEmpty()) {
		// custom sql
		file.renameTo(File("${file.absolutePath}.sql"))
	} else {
		// generated sql
		file.delete()
		transaction {
			exec("CREATE EXTENSION IF NOT EXISTS vector;")
			MigrationUtils.generateMigrationScript(
				*tables.toTypedArray(),
				scriptDirectory = folder.absolutePath,
				scriptName = name,
			)
		}
	}

	if (!applyMigration) return

	try {
		val flyway = Flyway.configure()
			.dataSource(env.read("DB_URL"), env.read("DB_USER"), env.read("DB_PW"))
			.locations("filesystem:$MIGRATIONS_DIRECTORY")
			.baselineOnMigrate(isBaseline) // Used when migrating an existing database for the first time
			.load()
		flyway.repair()
		flyway.migrate()
	} catch (e: Exception) {
		println("Error: ${e.message}")
		println("Recreating original file state")
		File("${file.absolutePath}.sql").renameTo(File("${file.absolutePath}.txt"))
		file.createNewFile()
	}
}

const val MIGRATIONS_DIRECTORY = "../migrations/apply" // Location of migration scripts