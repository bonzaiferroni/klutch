@file:OptIn(ExperimentalUuidApi::class)

package klutch.utils

import kampfire.api.TableId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import java.util.Base64
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

private val base64Encoder = Base64.getEncoder()

private fun UUID.toBase64(): String {
    // 16 bytes of UUID, packed big-endian
    val src = ByteArray(16)
    var msb = mostSignificantBits
    var lsb = leastSignificantBits

    for (i in 7 downTo 0) {
        src[i] = (msb and 0xFF).toByte()
        msb = msb ushr 8
    }
    for (i in 15 downTo 8) {
        src[i] = (lsb and 0xFF).toByte()
        lsb = lsb ushr 8
    }

    // Base64 output for 16 bytes is always 24 chars with "==", so 22 without padding
    val encoded = ByteArray(24)
    base64Encoder.encode(src, encoded)

    val out = CharArray(22)
    for (i in 0 until 22) {
        val c = encoded[i].toInt().toChar()
        out[i] = when (c) {
            '+' -> '-'
            '/' -> '_'
            else -> c
        }
    }
    return String(out)
}

private val B64 = Base64.getDecoder()

private fun String.fromBase64(): UUID {
    require(length == 22)

    // rebuild standard Base64 with padding
    val in24 = ByteArray(24)
    for (i in 0 until 22) {
        in24[i] = when (val c = this[i]) {
            '-' -> '+'.code
            '_' -> '/'.code
            else -> c.code
        }.toByte()
    }
    in24[22] = '='.code.toByte()
    in24[23] = '='.code.toByte()

    val bytes = B64.decode(in24)

    var msb = 0L
    var lsb = 0L
    for (i in 0..7)  msb = (msb shl 8) or (bytes[i].toLong() and 0xFF)
    for (i in 8..15) lsb = (lsb shl 8) or (bytes[i].toLong() and 0xFF)

    return UUID(msb, lsb)
}