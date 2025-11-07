package klutch.html

import kotlinx.html.*
import kotlinx.html.label as labelCore

fun FlowContent.checkBox(
    id: Id,
    label: String,
) {
    row(AlignItemsCenter) {
        checkBoxInput {
            this.id = id.value
            this.name = id.value
        }
        paragraph(label)
    }
}