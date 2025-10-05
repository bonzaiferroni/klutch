package klutch.html

interface CssClass {
    val value: String
}

fun modify(vararg cssClass: CssClass) =
    cssClass.map { it.value }.toSet()

fun modify(main: CssClass, modifiers: Set<String>? = null) =
    modifiers?.let { setOf(main.value) + it } ?: setOf(main.value)

// layouts
object Column : CssClass { override val value = "column" }
object Row : CssClass { override val value = "row" }
object Card : CssClass { override val value = "card" }

// utilities
object DisplayNone : CssClass { override val value = "display-none" }
object Bold: CssClass { override val value = "bold" }
object Flex1: CssClass { override val value = "flex-1" }
object Gap0: CssClass { override val value = "gap-0" }
object AlignItemsCenter: CssClass { override val value = "align-items-center" }
object FillWidth: CssClass { override val value = "fill-width" }