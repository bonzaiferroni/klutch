package klutch.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

data class CounterTrigger(
    val parentTable: Table,
    val parentIdColumn: Column<*>,
    val childTable: Table,
    val childFkColumn: Column<*>,
    val counterColumn: Column<*>,
)

fun JdbcTransaction.createCounterTrigger(config: CounterTrigger) {
    val funcName = "${config.childTable.tableName}_${config.counterColumn.name}"

    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_increment()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} + 1
            WHERE ${config.parentIdColumn.name} = NEW.${config.childFkColumn.name};
            RETURN NEW;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_decrement()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} - 1
            WHERE ${config.parentIdColumn.name} = OLD.${config.childFkColumn.name};
            RETURN OLD;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE TRIGGER trg_${funcName}_insert
        AFTER INSERT ON ${config.childTable.tableName}
        FOR EACH ROW EXECUTE FUNCTION ${funcName}_increment();
    """.trimIndent())

    exec("""
        CREATE OR REPLACE TRIGGER trg_${funcName}_delete
        AFTER DELETE ON ${config.childTable.tableName}
        FOR EACH ROW EXECUTE FUNCTION ${funcName}_decrement();
    """.trimIndent())
}