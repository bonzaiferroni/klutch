package klutch.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.box(
    id: Id,
    vararg modifiers: CssClass,
    content: DIV.() -> Unit = { },
) {
    div {
        this.id = id.value
        this.classes = modify(Box, *modifiers)
        content()
    }
}

fun FlowContent.box(
    vararg modifiers: CssClass,
    content: DIV.() -> Unit = { },
) {
    div {
        this.classes = modify(Box, *modifiers)
        content()
    }
}