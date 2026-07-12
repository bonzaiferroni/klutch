package klutch.db

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.jsonb
import koala.Image
import kotlinx.serialization.json.Json

fun Table.image(name: String) = jsonb<Image>(name, jsonColumnConfig)

val jsonColumnConfig = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
}