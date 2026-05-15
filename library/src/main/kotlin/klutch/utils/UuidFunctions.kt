@file:OptIn(ExperimentalUuidApi::class)

package klutch.utils

import kampfire.api.TableId
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun ExpressionWithColumnType<EntityID<Uuid>>.eq(value: Uuid) = this.eq(value)

fun ExpressionWithColumnType<EntityID<Uuid>>.eq(tableId: TableId<Uuid>) = this.eq(tableId.value)

