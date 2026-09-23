package klutch.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

data class CounterTrigger(
    val parentTable: IdTable<*>,
    val childTable: Table,
    val childFkColumn: Column<*>,
    val counterColumn: Column<*>,
    val countFilter: String? = null,
)

fun JdbcTransaction.createCounterTrigger(config: CounterTrigger) {
    val funcName = "${config.childTable.tableName}_${config.counterColumn.name}"
    val fk = config.childFkColumn.name
    // countFilter is written against NEW; the OLD form is for rows leaving the count
    val newFilterStart = config.countFilter?.let { "IF ($it) THEN" } ?: ""
    val oldFilterStart = config.countFilter?.let { "IF (${it.replace("NEW.", "OLD.")}) THEN" } ?: ""
    val filterEnd = config.countFilter?.let { "END IF;" } ?: ""

    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_increment()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            $newFilterStart
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} + 1
            WHERE ${config.parentTable.id.name} = NEW.$fk;
            $filterEnd
            RETURN NEW;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_decrement()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            $oldFilterStart
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} - 1
            WHERE ${config.parentTable.id.name} = OLD.$fk;
            $filterEnd
            RETURN OLD;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_move()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            $oldFilterStart
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} - 1
            WHERE ${config.parentTable.id.name} = OLD.$fk;
            $filterEnd
            $newFilterStart
            UPDATE ${config.parentTable.tableName}
            SET ${config.counterColumn.name} = ${config.counterColumn.name} + 1
            WHERE ${config.parentTable.id.name} = NEW.$fk;
            $filterEnd
            RETURN NEW;
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

    exec("""
        CREATE OR REPLACE TRIGGER trg_${funcName}_move
        AFTER UPDATE OF $fk ON ${config.childTable.tableName}
        FOR EACH ROW WHEN (OLD.$fk IS DISTINCT FROM NEW.$fk)
        EXECUTE FUNCTION ${funcName}_move();
    """.trimIndent())
}