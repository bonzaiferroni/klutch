package klutch.db

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

data class SyncValueTrigger<ID : Comparable<ID>, V>(
    val fkColumn: Column<*>,
    val syncedColumn: Column<*>,
    val sourceTable: IdTable<ID>,
    val sourceColumn: Column<V>,
)

fun <ID: Comparable<ID>, V> JdbcTransaction.createSyncValueTrigger(config: SyncValueTrigger<ID, V>) {
    val targetTable = config.fkColumn.table
    val funcName = "${targetTable.tableName}_sync_${config.syncedColumn.name}"

    // Sync on insert/update of the FK
    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            IF NEW.${config.fkColumn.name} IS NULL THEN
                IF TG_OP = 'UPDATE' AND OLD.${config.fkColumn.name} IS NOT NULL THEN
                    NEW.${config.syncedColumn.name} := NULL;
                END IF;
                RETURN NEW;
            END IF;
        
            SELECT ${config.sourceColumn.name}
            INTO NEW.${config.syncedColumn.name}
            FROM ${config.sourceTable.tableName}
            WHERE ${config.sourceTable.id.name} = NEW.${config.fkColumn.name};
            RETURN NEW;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE TRIGGER trg_$funcName
        BEFORE INSERT OR UPDATE OF ${config.fkColumn.name}
        ON ${targetTable.tableName}
        FOR EACH ROW EXECUTE FUNCTION ${funcName}();
    """.trimIndent())

    // Cascade when source value changes
    exec("""
        CREATE OR REPLACE FUNCTION ${funcName}_cascade()
        RETURNS TRIGGER AS ${'$'}${'$'}
        BEGIN
            UPDATE ${targetTable.tableName}
            SET ${config.syncedColumn.name} = NEW.${config.sourceColumn.name}
            WHERE ${config.fkColumn.name} = NEW.${config.sourceTable.id.name};
            RETURN NEW;
        END;
        ${'$'}${'$'} LANGUAGE plpgsql;
    """.trimIndent())

    exec("""
        CREATE OR REPLACE TRIGGER trg_${funcName}_cascade
        AFTER UPDATE OF ${config.sourceColumn.name}
        ON ${config.sourceTable.tableName}
        FOR EACH ROW EXECUTE FUNCTION ${funcName}_cascade();
    """.trimIndent())
}