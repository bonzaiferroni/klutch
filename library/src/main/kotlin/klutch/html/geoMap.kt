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

fun HEAD.geoMapResources() {
    script(src = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.js") { }
    link(href = "https://cdn.jsdelivr.net/npm/maplibre-gl@5.12.0/dist/maplibre-gl.css", "stylesheet")
}