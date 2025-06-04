package klutch.clients

import java.io.File

fun promptTemplate(
    path: String,
    vararg items: Pair<String, String>
): String {
    val text = File(path).readText()
    val sb = StringBuilder(text)
    items.forEach { (key, value) ->
        val templateKey = "<|$key|>"
        do {
            var index = sb.indexOf(templateKey)
            if (index >= 0)  {
                sb.replace(index, index + templateKey.length, value)
            }
        } while (index >= 0)
    }
    return sb.toString()
}