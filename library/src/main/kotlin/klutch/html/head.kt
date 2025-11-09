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
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js") { }
        block()
    }
}

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/css/$style" } }
}

fun HEAD.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/static/js/$script") {} }
}

fun HEAD.coreStyles() = styles(
    "reset.css",
    "styles.css",
    "typography.css",
    "button.css",
    "layout.css",
    "utilities.css",
    "animation.css",
    "tabs.css",
    "logo.css"
)

fun HEAD.coreScripts() = scripts(
    "utils.js",
    "tabs.js",
    "logo.js",
    "lottie.js"
)