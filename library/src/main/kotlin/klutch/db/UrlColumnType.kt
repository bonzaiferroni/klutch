package klutch.db

import kampfire.model.ScaledImage
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb

object UrlColumnType : ColumnType<Url>() {
    override fun sqlType(): String = "TEXT"
    override fun valueFromDB(value: Any): Url = Url(value as String)
    override fun notNullValueToDB(value: Url): Any = value.value
}

fun Table.url(name: String): Column<Url> = registerColumn(name, UrlColumnType)

fun Table.scaledImages(name: String): Column<ScaledImageArray> =
    jsonb<ScaledImageArray>(name, Json.Default)