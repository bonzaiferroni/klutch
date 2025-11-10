package klutch.html

import kotlinx.html.*

fun FlowContent.geoMap(
    width: String = "100%",
    height: String = "400px",
) {
    box(Id("geoMap")) {
        style = "width: $width; height: $height;"
    }
}