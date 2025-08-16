package klutch.html

open class Url internal constructor(
    val rawHref: String,
    junkParams: Set<String>?,
    disallowedPaths: Set<String>?,
) {
    val href: String

    val core: String
    val scheme: String
    val domain: String
    val domainSegments: Int
    val path: String
    val fullPath: String
    val pathSegments: Int
    val params: Map<String, String>
    val paramPath: String
    val fragment: String?
    val isRobotAllowed: Boolean?

    val authority get() = "$scheme://$domain"
    val length get() = toString().length

    init {
        if (!rawHref.startsWith("http"))
            throw IllegalArgumentException("Url must begin with http: $rawHref")
        val (beforeFragmentMarker, afterFragmentMarker) = rawHref.deconstruct("#")
        fragment = afterFragmentMarker
        val (beforeScheme, afterScheme) = beforeFragmentMarker.deconstruct("://")
        requireNotNull(afterScheme) { "Invalid Url: $rawHref" }
        scheme = beforeScheme

        val (beforePathMarker, afterPathMarker) = afterScheme.deconstruct("/")
        if (beforePathMarker.contains('@')) throw IllegalArgumentException("URL contains user info: $rawHref")
        domain = beforePathMarker.lowercase()
        if (domain.length > 100) throw IllegalArgumentException("Domain too long: $domain")
        if (domain.contains(":")) throw IllegalArgumentException("No ports allowed here sir")
        core = beforePathMarker.removePrefix("www.").lowercase()
        domainSegments = core.split('.').size
        if (domainSegments < 2) throw IllegalArgumentException("Invalid domain: $domain")
        val rawPath = afterPathMarker?.let { "/$it" } ?: "/"

        val (beforeParamMarker, afterParamMarker) = rawPath.deconstruct("?")
        path = beforeParamMarker
        pathSegments = path.split('/').filter { it.isNotEmpty() }.size
        var requiredParamPath = ""

        params = afterParamMarker?.split("&")?.mapNotNull { param ->
            val (key, value) = param.deconstruct("=")
            if (value == null) {
                if (requiredParamPath.isEmpty() && !afterParamMarker.contains('&')) {
                    requiredParamPath += "?$key" // some sites use ? like a fragment marker (c-span)
                }
                return@mapNotNull null
            }
            if (key.startsWith("utm_") || junkParams != null && key in junkParams) return@mapNotNull null

            requiredParamPath += if (requiredParamPath.isEmpty()) "?" else "&"
            requiredParamPath += param
            Pair(key, value)
        }?.toMap() ?: emptyMap()
        paramPath = requiredParamPath
        fullPath = "$path$paramPath${fragment?.let { "#$it" } ?: ""}"
        href = "$authority$fullPath"
        isRobotAllowed = disallowedPaths?.all { !path.startsWith(it) }
    }

    override fun toString() = href
    override fun equals(other: Any?) = other is Url && this.toString() == other.toString()
    override fun hashCode(): Int {
        var result = rawHref.hashCode()
        result = 31 * result + href.hashCode()
        return result
    }

    fun isMaybeSibling(other: Url): Boolean {
        val split1 = path.split('/')
        val root1 = split1.getOrNull(1) ?: return false
        val split2 = other.path.split('/')
        val root2 = split2.getOrNull(1) ?: return false
        // first segment same length or both have 1 segment
        return root1.length == root2.length || pathSegments == 1 && other.pathSegments == 1
    }
}

internal fun <T : Url> tryParseUrl(block: () -> T): T? {
    return try {
        block()
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun String.deconstruct(delimiter: String): Pair<String, String?> =
    this.split(delimiter, limit = 2).let { Pair(it[0], it.getOrNull(1)) }

internal fun String.appendIfRelativeHref(context: Url) = if (this.contains("://")) {
    this
} else {
    "${context.authority}${if (this.startsWith("/")) "" else "/"}${this}"
}

fun String.toUrl() = Url(this, null, null)

fun String.toUrlWithContext(context: Url) = this.appendIfRelativeHref(context).toUrl()

fun String.toUrlOrNull(): Url? = tryParseUrl { this.toUrl() }

fun String.toUrlWithContextOrNull(context: Url): Url? =
    tryParseUrl { this.appendIfRelativeHref(context).toUrl() }