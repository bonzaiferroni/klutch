package klutch.utils

import kotlinx.datetime.*

fun Clock.Companion.nowToLocalDateTimeUtc() = Clock.System.now().toLocalDateTimeUtc()
fun Instant.toLocalDateTimeUtc() = toLocalDateTime(TimeZone.UTC).let { time ->
    LocalDateTime(
        year = time.year,
        monthNumber = time.monthNumber,
        dayOfMonth = time.dayOfMonth,
        hour = time.hour,
        minute = time.minute,
        second = time.second,
        nanosecond = 0
    )
}

fun LocalDateTime.toInstantUtc() = this.toInstant(TimeZone.UTC)

fun Instant.Companion.tryParse(str: String) = try {
    parse(str)
} catch (e: Exception) {
    try {
        LocalDateTime.parse(str).toInstant(TimeZone.UTC)
    } catch (_: Exception) {
        null
    }
}

fun String.tryParseInstantOrNull() = Instant.tryParse(this)