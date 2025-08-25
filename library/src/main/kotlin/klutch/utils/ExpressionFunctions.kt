package klutch.utils

import kabinet.utils.toLocalDateTimeUtc
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import kotlin.time.Duration

// fun Column<LocalDateTime>.less(instant: Instant): LessOp = this.less(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.since(duration: Duration) = duration.let { (Clock.System.now() - it).toLocalDateTimeUtc() }
    .let { Op.build { this@since.greater(it) } }

fun ExpressionWithColumnType<LocalDateTime>.greaterEq(instant: Instant) =
    Op.build { this@greaterEq.greaterEq(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime?>.greaterEqNullable(instant: Instant) =
    Op.build { this@greaterEqNullable.greaterEq(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime>.greater(instant: Instant) =
    Op.build { this@greater.greater(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime?>.greaterNullable(instant: Instant) =
    Op.build { greater(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime>.less(instant: Instant) =
    Op.build { less(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime?>.lessNullable(instant: Instant) =
    Op.build { less(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime>.lessEq(instant: Instant) =
    Op.build { lessEq(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime?>.lessEqNullable(instant: Instant) =
    Op.build { lessEq(instant.toLocalDateTimeUtc()) }

fun <T> ExpressionWithColumnType<T>.isNullOrEq(t: T) =
    Op.build { isNull() or eq(t) }

fun <T> ExpressionWithColumnType<T>.isNullOrNeq(t: T) =
    Op.build { isNull() or neq(t) }

fun ExpressionWithColumnType<String>.eqLowercase(str: String) =
    Op.build { lowerCase() eq str.lowercase()}

fun ExpressionWithColumnType<LocalDateTime?>.betweenNullable(start: Instant, end: Instant) =
    Op.build { greaterEqNullable(start) and lessNullable(end) }