package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class HttpSource : Source {
    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/1"
        val bytes = java.security.MessageDigest.getInstance("MD5").digest(key.toByteArray())
        java.nio.ByteBuffer.wrap(bytes).long
    }

    abstract val baseUrl: String
    open val supportsLatest: Boolean = true

    val network: NetworkHelper = NetworkHelper()
    open val client: OkHttpClient get() = network.client

    open fun headersBuilder(): Headers.Builder = Headers.Builder()
    val headers: Headers by lazy { headersBuilder().build() }

    abstract fun popularMangaRequest(page: Int): Request
    abstract fun latestUpdatesRequest(page: Int): Request
    abstract fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request
}
