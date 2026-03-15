package klutch.db

import kabinet.utils.toLocalDateTimeUtc
import kampfire.api.TableId
import klutch.db.tables.RefreshTokenTable.default
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import java.util.UUID

fun Column<LocalDateTime>.defaultNow() = default(Clock.System.now().toLocalDateTimeUtc())

fun Column<EntityID<UUID>>.inList(idList: List<TableId<String>>) = Op.build {
    inList(idList.map { it.toUUID() })
}