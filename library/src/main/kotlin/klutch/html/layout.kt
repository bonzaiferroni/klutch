package klutch.html

import kotlinx.html.*

fun FlowContent.column(
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        this.classes = modify(Column, modifiers)
        content()
    }
}

fun FlowContent.row(
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div(Row.value) {
        this.classes = modify(Row, modifiers)
        content()
    }
}

fun FlowContent.card(
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        this.classes = modify(Card, modifiers)
        content()
    }
}

fun FlowContent.box(
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        modifiers?.let { this.classes = it  }
        content()
    }
}