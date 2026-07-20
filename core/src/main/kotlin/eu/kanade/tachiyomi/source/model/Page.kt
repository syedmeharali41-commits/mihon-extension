package eu.kanade.tachiyomi.source.model

open class Page(
    val index: Int,
    val url: String = "",
    var imageUrl: String? = null,
    var uri: Any? = null
)
