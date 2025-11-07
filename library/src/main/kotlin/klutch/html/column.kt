package klutch.html

import kotlinx.html.*

fun FlowContent.column(
    id: Id,
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        this.classes = modify(Column, *modifiers)
        content()
    }
}

fun FlowContent.column(
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.classes = modify(Column, *modifiers)
        content()
    }
}