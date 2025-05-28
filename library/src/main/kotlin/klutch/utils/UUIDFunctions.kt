package klutch.utils

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

fun String.toUUID(): UUID = UUID.fromString(this)

fun ExpressionWithColumnType<EntityID<UUID>>.eq(value: String) = this.eq(value.toUUID())