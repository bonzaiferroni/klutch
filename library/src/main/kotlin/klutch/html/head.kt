package klutch.html

import kotlinx.html.*

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/css/$style" } }
}

fun HEAD.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/static/js/$script") {} }
}

fun HEAD.coreStyles() = styles("reset.css", "styles.css", "button.css", "layout.css", "utilities.css", "animation.css", "tabs.css")

fun HEAD.coreScripts() = scripts("helpers.js", "tabs.js")