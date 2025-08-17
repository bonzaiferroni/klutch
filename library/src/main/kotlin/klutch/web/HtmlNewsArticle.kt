package klutch.web

import kotlinx.serialization.Serializable

@Serializable
data class MetaNewsArticle(
    val headline: String? = null,
    val dateline: String? = null,
    val datePublished: String? = null,
    val dateModified: String? = null,
    val author: List<NewsAuthor>? = null,
    val publisher: Publisher? = null,
    val articleBody: String? = null,
    val articleSection: List<String>? = null,
    val wordCount: Int? = null,
    val keywords: List<String>? = null,
    val abstract: String? = null,
    val alternativeHeadline: String? = null,
    val description: String? = null,
    val url: String? = null,
    val isAccessibleForFree: Boolean? = null,
    val text: String? = null,
    val thumbnailUrl: String? = null,
    val image: List<Image>? = null,
    val inLanguage: String? = null,
    val commentCount: Int? = null,
)

@Serializable
data class NewsAuthor(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null,
    val sameAs: List<String>? = null,
    val image: Image? = null,
)

@Serializable
data class Publisher(
    val name: String? = null,
    val logo: Image? = null
)

@Serializable
data class Image(
    val width: Int? = null,
    val height: Int? = null,
    val url: String? = null,
    val caption: String? = null,
    val creditText: String? = null,
)

@Serializable
data class QuantitativeValue(
    val unitCode: String? = null,
    val value: Int? = null,
)