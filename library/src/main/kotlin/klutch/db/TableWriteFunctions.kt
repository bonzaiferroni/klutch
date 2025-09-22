package klutch.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ISqlExpressionBuilder
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update

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

fun <Id: Comparable<Id>, T: IdTable<Id>, Item> T.batchUpdate(
    items: List<Item>,
    provideId: (Item) -> Id,
    updateItem: UpdateBuilder<*>.(Item) -> Unit,
) = {
    var total = 0
    BatchUpdateStatement(this).apply {
        items.forEach { item ->
            addBatch(EntityID(provideId(item), this@batchUpdate))
            updateItem(item)
        }
        total = execute(TransactionManager.current()) ?: 0
    }
    total
}

fun <Id : Comparable<Id>, T : IdTable<Id>> T.deleteSingle(
    block: ISqlExpressionBuilder.(T) -> Op<Boolean>
) = deleteWhere{ it.block(this@deleteSingle) } == 1