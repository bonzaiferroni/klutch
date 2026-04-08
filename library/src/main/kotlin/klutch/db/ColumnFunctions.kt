package klutch.db

import kabinet.utils.toLocalDateTimeUtc
import kampfire.api.TableId
import klutch.db.tables.BasicUserTable.default
import klutch.utils.toUUID
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.inList
import java.util.UUID
import kotlin.time.Clock

fun Column<LocalDateTime>.defaultNow() = default(Clock.System.now().toLocalDateTimeUtc())

fun Column<EntityID<UUID>>.inList(idList: List<TableId<String>>) = this.inList(idList.map { it.toUUID() })