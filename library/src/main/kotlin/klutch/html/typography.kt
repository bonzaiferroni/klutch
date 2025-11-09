package klutch.html

import kotlinx.html.*

fun FlowContent.label(
    content: String,
    vararg modifiers: CssClass?,
    block: (DIV.() -> Unit)? = null
) {
    div {
        modify(*modifiers)
        block?.let { it() }
        +content
    }
}

fun FlowContent.paragraph(
    content: String,
    vararg modifiers: CssClass,
) {
    p {
        modify(Row, *modifiers)
        +content
    }
}

fun FlowContent.heading1(
    content: String,
    vararg modifiers: CssClass,
) {
    h1 {
        modify(Row, *modifiers)
        +content
    }
}

fun FlowContent.heading2(
    content: String,
    vararg modifiers: CssClass,
) {
    h2 {
        modify(Row, *modifiers)
        +content
    }
}

fun FlowContent.heading3(
    content: String,
    vararg modifiers: CssClass,
) {
    h3 {
        modify(Row, *modifiers)
        +content
    }
}

fun FlowContent.heading4(
    content: String,
    vararg modifiers: CssClass,
) {
    h4 {
        modify(Row, *modifiers)
        +content
    }
}

fun FlowContent.heading5(
    content: String,
    vararg modifiers: CssClass,
) {
    h5 {
        modify(Row, *modifiers)
        +content
    }
}