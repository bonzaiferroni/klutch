package klutch.html

import kotlinx.html.*

fun FlowContent.paragraph(
    content: String,
    modifiers: Set<String>? = null,
    id: Id? = null,
) {
    p {
        id?.let { this.id = it.value }
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    id: String? = null,
    classes: String? = null
) {
    h1(classes) {
        +content
        id?.let { this.id = it }
    }
}

fun FlowContent.heading2(
    content: String,
    id: Id? = null,
    classes: String? = null
) {
    h2(classes) {
        +content
        id?.let { this.id = it.value }
    }
}

fun FlowContent.heading3(
    content: String,
    id: Id? = null,
    classes: String? = null
) {
    h3(classes) {
        +content
        id?.let { this.id = it.value }
    }
}