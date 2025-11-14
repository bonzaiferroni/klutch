package klutch.db

import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.RefreshTokenTable.default
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Column

fun Column<LocalDateTime>.defaultNow() = default(Clock.System.now().toLocalDateTimeUtc())