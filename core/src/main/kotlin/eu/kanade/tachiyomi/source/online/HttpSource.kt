package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
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
    open fun popularMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Not implemented")

    abstract fun latestUpdatesRequest(page: Int): Request
    open fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    abstract fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request
    open fun searchMangaParse(response: Response): MangasPage = popularMangaParse(response)

    open fun mangaDetailsRequest(manga: SManga): Request = Request.Builder().url(baseUrl + manga.url).headers(headers).build()
    open fun mangaDetailsParse(response: Response): SManga = throw UnsupportedOperationException("Not implemented")

    open fun chapterListRequest(manga: SManga): Request = mangaDetailsRequest(manga)
    open fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException("Not implemented")

    open fun pageListRequest(chapter: SChapter): Request = Request.Builder().url(baseUrl + chapter.url).headers(headers).build()
    open fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException("Not implemented")

    open fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not implemented")
}
