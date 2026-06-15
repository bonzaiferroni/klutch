@file:OptIn(ExperimentalUuidApi::class)

package klutch.utils

import kampfire.api.Slug
import kampfire.api.TableId
import kampfire.api.Username
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// fun ExpressionWithColumnType<EntityID<Uuid>>.eq(value: Uuid) = this.eq(value)
@JvmName("eqTableId")
fun ExpressionWithColumnType<EntityID<Uuid>>.eq(tableId: TableId<Uuid>) = this.eq(tableId.value)
@JvmName("nullableEqTableId")
fun ExpressionWithColumnType<EntityID<Uuid>?>.eq(tableId: TableId<Uuid>) = this.eq(tableId.value)

@JvmName("slugEq")
fun ExpressionWithColumnType<String>.eq(slug: Slug) = this.eq(slug.value)
@JvmName("nullableSlugEq")
fun ExpressionWithColumnType<String?>.eq(slug: Slug) = this.eq(slug.value)

@JvmName("usernameEq")
fun ExpressionWithColumnType<String>.eq(username: Username) = this.eq(username.value)
@JvmName("nullableUsernameEq")
fun ExpressionWithColumnType<String?>.eq(username: Username) = this.eq(username.value)