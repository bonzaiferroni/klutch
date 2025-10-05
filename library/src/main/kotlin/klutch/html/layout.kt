package klutch.html

import kotlinx.html.*

fun FlowContent.column(
    id: Id? = null,
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        id?.let { this.id = it.value }
        this.classes = modify(Column, modifiers)
        content()
    }
}

fun FlowContent.row(
    id: Id? = null,
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div(Row.value) {
        id?.let { this.id = it.value }
        modifiers?.let { this.classes = it  }
        content()
    }
}

fun FlowContent.card(
    id: Id? = null,
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        id?.let { this.id = it.value }
        this.classes = modify(Card, modifiers)
        content()
    }
}

fun FlowContent.box(
    id: Id? = null,
    modifiers: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        id?.let { this.id = it.value }
        modifiers?.let { this.classes = it  }
        content()
    }
}

fun FlowContent.box(
    modifier: CssClass,
    content: DIV.() -> Unit,
) {
    div(modifier.value) {
        content()
    }
}