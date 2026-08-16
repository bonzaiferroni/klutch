package klutch.db

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.jsonb
import koala.Image
import kotlinx.serialization.json.Json

fun Table.image(name: String) = jsonb<Image>(name, jsonbConfig)

val jsonbConfig = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
}