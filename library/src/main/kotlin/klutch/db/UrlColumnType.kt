package klutch.db

import kampfire.model.ScaledImage
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.jsonb

object UrlColumnType : ColumnType<Url>() {
    override fun sqlType(): String = "TEXT"
    override fun valueFromDB(value: Any): Url = Url(value as String)
    override fun notNullValueToDB(value: Url): Any = value.value
}

fun Table.url(name: String): Column<Url> = registerColumn(name, UrlColumnType)

fun Table.scaledImages(name: String): Column<ScaledImageArray> =
    jsonb<ScaledImageArray>(name, Json.Default)