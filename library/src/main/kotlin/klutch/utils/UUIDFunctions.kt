package klutch.utils

import kabinet.db.TableId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import java.util.UUID

fun ExpressionWithColumnType<EntityID<UUID>>.eq(value: String) = this.eq(value.fromStringId())

fun <T : EntityID<UUID>?> ExpressionWithColumnType<T>.eq(tableId: TableId<String>?) =
    tableId?.let { this.eq(it.value.fromStringId()) } ?: this.isNull()

fun UUID.toStringId() = this.toString()

fun String.fromStringId(): UUID = UUID.fromString(this)

fun TableId<String>.toUUID() = this.value.fromStringId()

//fun UUID.toLongPair(): Pair<Long, Long> =
//    mostSignificantBits to leastSignificantBits
//
//fun Pair<Long, Long>.toUuid(): UUID =
//    UUID(first, second)
//
//fun UUID.toStringId() = toLongPair().let { "${it.first.toBase62()}-${it.second.toBase62()}" }
//
//fun String.fromStringId() = this.split("-")
//    .let {
//        if (it.size != 2) error("Not a UUID stringId: $this")
//        it[0].fromBase62() to it[1].fromBase62()
//    }
//    .toUuid()