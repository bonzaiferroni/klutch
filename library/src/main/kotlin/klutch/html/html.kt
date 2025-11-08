package klutch.html

import kotlinx.html.*

fun HTML.head(
    title: String,
    block: HEAD.() -> Unit,
) {
    head {
        title { +title }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1" }
        coreStyles()
        coreScripts()
        block()
    }
}