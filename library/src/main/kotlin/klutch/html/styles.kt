package klutch.html

interface CssClass {
    val value: String
}

@JvmInline
value class Css(override val value: String): CssClass

fun modify(vararg cssClass: CssClass?) =
    cssClass.mapNotNull { it?.value }.toSet()

// layouts
object Column : CssClass { override val value = "layout-column" }
object Card : CssClass { override val value = "layout-card" }
object Box : CssClass { override val value = "layout-box"}

// utilities
object DisplayNone : CssClass { override val value = "display-none" }
object Bold: CssClass { override val value = "bold" }
object Flex1: CssClass { override val value = "flex-1" }
object Gap0: CssClass { override val value = "gap-0" }
object AlignItemsCenter: CssClass { override val value = "align-items-center" }
object FillWidth: CssClass { override val value = "fill-width" }
object DimText: CssClass { override val value = "dim-label" }

// animation
object Fade: CssClass { override val value = "fade" }
object Show: CssClass { override val value = "show" }
object FadeStack: CssClass { override val value = "fade-stack" }