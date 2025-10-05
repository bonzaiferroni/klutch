package klutch.html

import kotlinx.html.*
import kotlinx.html.button as buttonCore

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String,
    modifier: CssClass? = null,
    content: BUTTON.() -> Unit = {},
) {
    buttonCore(classes = modifier?.value) {
        this.onClick = onClick
        +text
        content()
    }
}