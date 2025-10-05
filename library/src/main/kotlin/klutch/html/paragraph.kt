package klutch.html

import kotlinx.html.*
import kotlinx.html.p as coreParagraph

fun FlowContent.p(
    content: String,
    id: String? = null,
    classes: String? = null,
) {
    coreParagraph(classes) {
        id?.let { this.id = it }
        +content
    }
}