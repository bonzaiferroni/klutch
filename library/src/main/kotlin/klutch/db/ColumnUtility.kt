package klutch.db

import kabinet.utils.toLocalDateTimeUtc
import kampfire.api.TableId
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.inList
import kotlin.time.Clock
import kotlin.uuid.Uuid

// fun Column<LocalDateTime>.defaultNow() = default(Clock.System.now().toLocalDateTimeUtc())

// fun Column<EntityID<Uuid>>.inList(idList: List<TableId<Uuid>>) = this.inList(idList.map { it.value })