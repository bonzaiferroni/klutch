package klutch.utils

import kabinet.utils.toLocalDateTimeUtc
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import kotlin.time.Duration

// fun Column<LocalDateTime>.less(instant: Instant): LessOp = this.less(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.since(duration: Duration) = duration.let { (Clock.System.now() - it).toLocalDateTimeUtc() }
    .let { Op.build { this@since.greater(it) } }

fun ExpressionWithColumnType<LocalDateTime>.greater(instant: Instant) =
    Op.build { this@greater.greater(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime>.less(instant: Instant) =
    Op.build { this@less.less(instant.toLocalDateTimeUtc()) }

fun ExpressionWithColumnType<LocalDateTime>.lessEq(instant: Instant) =
    Op.build { this@lessEq.lessEq(instant.toLocalDateTimeUtc()) }

fun <T> ExpressionWithColumnType<T>.isNullOrEq(t: T) =
    Op.build { this@isNullOrEq.isNull() or this@isNullOrEq.eq(t) }

fun <T> ExpressionWithColumnType<T>.isNullOrNeq(t: T) =
    Op.build { this@isNullOrNeq.isNull() or this@isNullOrNeq.neq(t) }

fun ExpressionWithColumnType<String>.eqLowercase(str: String) =
    Op.build { this@eqLowercase.lowerCase() eq str.lowercase()}