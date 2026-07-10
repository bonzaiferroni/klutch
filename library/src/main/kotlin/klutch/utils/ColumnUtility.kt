@file:OptIn(ExperimentalUuidApi::class)

package klutch.utils

import kabinet.utils.toLocalDateTimeUtc
import kampfire.api.Slug
import kampfire.api.TableId
import kampfire.api.Username
import kampfire.api.toMarkdown
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// fun ExpressionWithColumnType<EntityID<Uuid>>.eq(value: Uuid) = this.eq(value)
@JvmName("eqTableUuid")
fun ExpressionWithColumnType<EntityID<Uuid>>.eq(tableId: TableId<Uuid>) = this.eq(tableId.value)
@JvmName("eqTableUuidNullable")
fun ExpressionWithColumnType<EntityID<Uuid>?>.eq(tableId: TableId<Uuid>) = this.eq(tableId.value)

@JvmName("eqTableLong")
fun ExpressionWithColumnType<EntityID<Long>>.eq(tableId: TableId<Long>) = this.eq(tableId.value)
@JvmName("eqTableLongNullable")
fun ExpressionWithColumnType<EntityID<Long>?>.eq(tableId: TableId<Long>) = this.eq(tableId.value)

@JvmName("eqSlug")
fun ExpressionWithColumnType<String>.eq(slug: Slug) = this.eq(slug.value)
@JvmName("eqSlugNullable")
fun ExpressionWithColumnType<String?>.eq(slug: Slug) = this.eq(slug.value)

@JvmName("eqUsername")
fun ExpressionWithColumnType<String>.eq(username: Username) = this.eq(username.value)
@JvmName("eqUsernameNullable")
fun ExpressionWithColumnType<String?>.eq(username: Username) = this.eq(username.value)

fun Column<LocalDateTime>.defaultNow() = with(table) { default(Clock.System.now().toLocalDateTimeUtc()) }

fun Column<EntityID<Uuid>>.inList(idList: List<TableId<Uuid>>) = this.inList(idList.map { it.value })

fun Column<String>.transformMarkdown() = with(table) { transform(wrap = { it.toMarkdown() }, unwrap = { it.value }) }
//fun <T: TableId<Uuid>> Column<EntityID<Uuid>>.transformId(toValue: (EntityID<Uuid>) -> T) = transform(
//    wrap = { it.value }, unwrap = { toValue(it) }
//)