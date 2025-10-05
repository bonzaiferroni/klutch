package klutch.html

import kotlinx.html.*
import kotlinx.html.label as labelCore

fun FlowOrInteractiveOrPhrasingContent.checkBox(
    id: Id,
    label: String,
) {
    labelCore {
        checkBoxInput {
            this.id = id.value
            this.name = id.value
        }
        +label
    }
}