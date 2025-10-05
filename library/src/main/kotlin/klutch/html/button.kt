package klutch.html

import kotlinx.html.*

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String,
    classes: Classes? = null,
    content: BUTTON.() -> Unit = {},
) {
    button(classes = classes?.value) {
        this@button.onClick = onClick
        +text
        content()
    }
}