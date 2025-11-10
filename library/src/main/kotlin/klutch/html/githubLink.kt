package klutch.html

import kotlinx.html.A
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.SPAN
import kotlinx.html.a
import kotlinx.html.span

fun FlowOrPhrasingContent.githubLink(
    name: String,
    user: String,
    repo: String = name,
) {
    a("https://github.com/$user/$repo") {
        target = "_blank"
        rel = "noopener noreferrer"
        +name
    }
}