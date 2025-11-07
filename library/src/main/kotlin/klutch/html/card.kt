package klutch.html

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id

fun FlowContent.card(
    id: Id,
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.id = id.value
        this.classes = modify(Card, *modifiers)
        content()
    }
}

fun FlowContent.card(
    vararg modifiers: CssClass,
    content: DIV.() -> Unit,
) {
    div {
        this.classes = modify(Card, *modifiers)
        content()
    }
}