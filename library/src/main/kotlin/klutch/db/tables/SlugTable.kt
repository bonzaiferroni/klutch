package klutch.db.tables

import kampfire.api.Slug
import kampfire.api.toSlug
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow

interface SlugTable {
    val slug: Column<String>
    val pastSlug: Column<String?>? get() = null
}

fun ResultRow.toSlugRecord(slugColumn: Column<String>, pastSlugColumn: Column<String?>?) = SlugRecord(
    slug = this[slugColumn].toSlug(),
    pastSlug = pastSlugColumn?.let { this[it]?.toSlug()  },
)

data class SlugRecord(val slug: Slug, val pastSlug: Slug? = null)