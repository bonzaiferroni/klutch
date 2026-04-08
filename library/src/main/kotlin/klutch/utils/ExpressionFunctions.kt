package klutch.utils

import kabinet.utils.toLocalDateTimeUtc
import klutch.utils.eq
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

// fun Column<LocalDateTime>.less(instant: Instant): LessOp = this.less(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.since(duration: Duration) = duration.let { (Clock.System.now() - it).toLocalDateTimeUtc() }
    .let { this@since.greater(it) }

fun ExpressionWithColumnType<LocalDateTime>.greaterEq(instant: Instant) =
    this@greaterEq.greaterEq(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime?>.greaterEqNullable(instant: Instant) =
    this@greaterEqNullable.greaterEq(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.greater(instant: Instant) =
    this@greater.greater(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime?>.greaterNullable(instant: Instant) = greater(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.less(instant: Instant) = less(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime?>.lessNullable(instant: Instant) =
    less(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime>.lessEq(instant: Instant) =
    lessEq(instant.toLocalDateTimeUtc())

fun ExpressionWithColumnType<LocalDateTime?>.lessEqNullable(instant: Instant) =
    lessEq(instant.toLocalDateTimeUtc())

fun <T> ExpressionWithColumnType<T>.isNullOrEq(t: T) = isNull() or eq(t)

fun <T> ExpressionWithColumnType<T>.isNullOrNeq(t: T) = isNull() or neq(t)

fun ExpressionWithColumnType<String>.eqLowercase(str: String) = lowerCase() eq str.lowercase()

fun ExpressionWithColumnType<LocalDateTime?>.betweenNullable(start: Instant, end: Instant) =
    greaterEqNullable(start) and lessNullable(end)