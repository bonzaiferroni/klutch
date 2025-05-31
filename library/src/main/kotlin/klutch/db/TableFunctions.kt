package klutch.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

fun <T : Table> T.read(
    columns: List<Column<*>> = this.columns,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@read) }

fun <T : Table> T.readSingleOrNull(
    columns: List<Column<*>> = this.columns,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readSingleOrNull) }
    .singleOrNull()

fun <T : Table> T.readSingle(
    columns: List<Column<*>> = this.columns,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readSingle) }
    .single()

fun <T : Table> T.readFirstOrNull(
    columns: List<Column<*>> = this.columns,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readFirstOrNull) }
    .firstOrNull()

fun <T : Table> T.readFirst(
    columns: List<Column<*>> = this.columns,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readFirst) }
    .first()

fun <T : Table, C> T.readColumn(
    column: Column<C>,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(column)
    .where { block(this@readColumn) }
    .map { it[column] }

fun <T : Table, C> T.readValue(
    column: Column<C>,
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(column)
    .where { block(this@readValue) }
    .single()[column]

fun <T : Table> T.readCount(
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.selectAll()
    .where { block(this@readCount) }
    .count().toInt()

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readById(
    id: Id,
    columns: List<Column<*>> = this.columns,
) = this.select(columns)
    .where { this@readById.id.eq(id) }
    .first()

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readByIdOrNull(
    id: Id,
    columns: List<Column<*>> = this.columns,
) = this.select(columns)
    .where { this@readByIdOrNull.id.eq(id) }
    .firstOrNull()

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readId(
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(this.id)
    .where { block(this@readId) }
    .first()[this.id].value

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readIdOrNull(
    block: SqlExpressionBuilder.(T) -> Op<Boolean>
) = this.select(this.id)
    .where { block(this@readIdOrNull) }
    .firstOrNull()?.let { it[this.id].value }

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readAll() = this.select(this.columns)

fun <Id : Comparable<Id>, T : IdTable<Id>> T.updateById(
    id: Id,
    block: T.(UpdateStatement) -> Unit
) = this.update(where = { this@updateById.id.eq(id) }, body = block)

fun <Id : Comparable<Id>, T : IdTable<Id>> T.updateSingleWhere(
    where: SqlExpressionBuilder.(T) -> Op<Boolean>,
    block: T.(UpdateStatement) -> Unit
) = this.read(listOf(id), block = where).let {
    if (it.count() != 1L) return@let null
    val id = it.first()[id].value
    updateById(id, block = block)
    id
}

fun <Id : Comparable<Id>, T : IdTable<Id>> T.updateOrInsert(
    where: SqlExpressionBuilder.(T) -> Op<Boolean>,
    block: T.(UpdateOrInsertArgs) -> Unit
) = this.read(listOf(id), block = where).let {
    if (it.count() != 1L) return@let null
    val id = it.first()[id].value
    updateById(id) { block(UpdateOrInsertArgs(it, false))}
    id
} ?: this.insertAndGetId { block(UpdateOrInsertArgs(it, true)) }.value

data class UpdateOrInsertArgs(
    val row: UpdateBuilder<*>,
    val isInsert: Boolean
)

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readIdOrInsert(
    where: SqlExpressionBuilder.(T) -> Op<Boolean>,
    block: T.(InsertStatement<EntityID<Id>>) -> Unit
) = this.read(listOf(this.id), block = where).let {
    if (it.count() != 1L) return@let null
    it.first()[id].value
} ?: this.insertAndGetId(block).value