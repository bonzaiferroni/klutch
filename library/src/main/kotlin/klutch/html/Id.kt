package klutch.html

import kotlinx.html.*

@JvmInline
value class Id(val value: String)

fun CoreAttributeGroupFacade.set(id: Id) {
    this.id = id.value
}