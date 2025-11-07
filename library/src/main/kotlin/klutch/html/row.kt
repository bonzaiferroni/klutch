package klutch.html

import kotlinx.html.*

object Row : CssClass { override val value = "layout-row" }

fun FlowContent.row(
    id: Id,
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        this.classes = modify(Row, *modifiers)
        content()
    }
}

fun FlowContent.row(
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.classes = modify(Row, *modifiers)
        content()
    }
}