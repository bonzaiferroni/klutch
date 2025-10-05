package klutch.html

import kotlinx.html.*

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/$style" } }
}

fun HEAD.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/static/$script") {} }
}

fun HEAD.coreStyles() = styles("styles.css", "button.css", "layout.css", "utilities.css")

fun HEAD.coreScripts() = scripts("helpers.js")