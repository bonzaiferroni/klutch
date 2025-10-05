package klutch.html

import kotlinx.html.*

fun HEAD.styles(vararg styles: String) {
    styles.forEach { style -> link { rel = "stylesheet"; href = "/static/$style" } }
}

fun HEAD.scripts(vararg scripts: String) {
    scripts.forEach { script -> script(src = "/static/$script") {} }
}