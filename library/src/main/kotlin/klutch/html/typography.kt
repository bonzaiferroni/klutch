package klutch.html

import kotlinx.html.*

fun FlowContent.label(
    content: String,
    vararg modifiers: CssClass?,
    block: (DIV.() -> Unit)? = null
) {
    div {
        this.classes = modify(*modifiers)
        block?.let { it() }
        +content
    }
}

fun FlowContent.paragraph(
    content: String,
    modifiers: Set<String>? = null,
) {
    p {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    modifiers: Set<String>? = null,
) {
    h1 {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    modifiers: Set<String>? = null,
) {
    h2 {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    modifiers: Set<String>? = null,
) {
    h3 {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    modifiers: Set<String>? = null,
) {
    h4 {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    modifiers: Set<String>? = null,
) {
    h5 {
        modifiers?.let { this.classes = modifiers }
        +content
    }
}