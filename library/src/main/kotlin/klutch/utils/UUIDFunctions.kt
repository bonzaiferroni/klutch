@file:OptIn(ExperimentalUuidApi::class)

package klutch.utils

import kabinet.db.TableId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun ExpressionWithColumnType<EntityID<UUID>>.eq(value: String) = this.eq(value.toUUID())

@JvmName("eqStringId")
fun <T : EntityID<UUID>?> ExpressionWithColumnType<T>.eq(tableId: TableId<String>?) =
    tableId?.let { this.eq(it.value.toUUID()) } ?: this.isNull()

@JvmName("eqUuidId")
fun <T : EntityID<UUID>?> ExpressionWithColumnType<T>.eq(tableId: TableId<Uuid>?) =
    tableId?.let { this.eq(it.value.toUUID()) } ?: this.isNull()

fun UUID.toStringId() = this.toString()

fun String.toUUID(): UUID = UUID.fromString(this)

@JvmName("toUUIDfromString")
fun TableId<String>.toUUID() = this.value.toUUID()

@JvmName("toUUIDfromUuid")
fun TableId<Uuid>.toUUID(): UUID = UUID.fromString(this.value.toString())

fun UUID.toUuid(): Uuid = Uuid.parse(this.toString())

fun Uuid.toUUID(): UUID = UUID.fromString(this.toString())



//fun UUID.toLongPair(): Pair<Long, Long> =
//    mostSignificantBits to leastSignificantBits
//
//fun Pair<Long, Long>.toUuid(): UUID =
//    UUID(first, second)
//
//fun UUID.toStringId() = toLongPair().let { "${it.first.toBase62()}-${it.second.toBase62()}" }
//
//fun String.toUUID() = this.split("-")
//    .let {
//        if (it.size != 2) error("Not a UUID stringId: $this")
//        it[0].fromBase62() to it[1].fromBase62()
//    }
//    .toUuid()