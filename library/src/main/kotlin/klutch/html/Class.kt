package klutch.html

@JvmInline
value class Classes(val value: String)

interface CssClass {
    val value: String
}

fun classes(vararg cssClass: CssClass) = cssClass.map { it.value }.toSet()

object Column : CssClass { override val value = "column" }
object Hidden : CssClass { override val value = "hidden" }