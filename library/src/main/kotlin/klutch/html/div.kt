package klutch.html

import kotlinx.html.*
import kotlinx.html.div as coreDiv


fun FlowContent.div(
    id: Id? = null,
    classSet: Set<String>? = null,
    content: DIV.() -> Unit,
) {
    coreDiv {
        id?.let { this.id = it.value }
        classSet?.let { this.classes = it  }
        content()
    }
}

fun FlowContent.div(
    classes: Classes,
    content: DIV.() -> Unit,
) {
    coreDiv(classes.value) {
        content()
    }
}