package klutch.db

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

fun <T : ColumnSet> T.read(
    columns: List<Column<*>> = this.columns,
    block: (T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@read) }

fun <T : ColumnSet> T.readSingleOrNull(
    columns: List<Column<*>> = this.columns,
    block: (T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readSingleOrNull) }
    .singleOrNull()

fun <T : ColumnSet> T.readSingle(
    columns: List<Column<*>> = this.columns,
    block: (T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readSingle) }
    .single()

fun <T : ColumnSet> T.readFirstOrNull(
    columns: List<Column<*>> = this.columns,
    block: (T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readFirstOrNull) }
    .firstOrNull()

fun <T : ColumnSet> T.readFirst(
    columns: List<Column<*>> = this.columns,
    block: (T) -> Op<Boolean>
) = this.select(columns)
    .where { block(this@readFirst) }
    .first()

fun <T : ColumnSet, C> T.readColumn(
    column: Column<C>,
    block: (T) -> Op<Boolean>
) = this.select(column)
    .where { block(this@readColumn) }
    .map { it[column] }

fun <T : ColumnSet, C> T.readValue(
    column: Column<C>,
    block: (T) -> Op<Boolean>
) = this.select(column)
    .where { block(this@readValue) }
    .single()[column]

fun <T : ColumnSet> T.count(
    block: (T) -> Op<Boolean>
) = this.selectAll()
    .where { block(this@count) }
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
    block: (T) -> Op<Boolean>
) = this.select(this.id)
    .where { block(this@readId) }
    .first()[this.id].value

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readIdOrNull(
    block: (T) -> Op<Boolean>
) = this.select(this.id)
    .where { block(this@readIdOrNull) }
    .firstOrNull()?.let { it[this.id].value }

fun <Id : Comparable<Id>, T : IdTable<Id>> T.readAll() = this.select(this.columns)