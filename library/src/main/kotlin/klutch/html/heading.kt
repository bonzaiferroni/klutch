package klutch.html

import kotlinx.html.*

fun FlowContent.h1(
    content: String,
    id: String? = null,
    classes: String? = null
) {
    h1(classes) {
        +content
        id?.let { this@h1.id = it }
    }
}

fun FlowContent.h2(
    content: String,
    id: Id? = null,
    classes: String? = null
) {
    h2(classes) {
        +content
        id?.let { this@h2.id = it.value }
    }
}