package eu.kanade.tachiyomi.extension.en.ybxmanga

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class YbxManga : ParsedHttpSource() {

    override val name = "YBX Manga"

    override val baseUrl = "https://www.ybxmanga.in"

    private val apiBaseUrl = "https://wheat-buffalo-825600.hostingersite.com/api/v1"
    private val cdnHost = "https://wheat-buffalo-825600.hostingersite.com"

    override val lang = "en"

    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        .add("Accept", "application/json, text/plain, */*")
        .add("Referer", "$baseUrl/")

    // --- Popular Manga ---

    override fun popularMangaRequest(page: Int): Request {
        return GET("$apiBaseUrl/manga?sort=popular&page=$page", headers)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val jsonStr = response.body?.string() ?: return MangasPage(emptyList(), false)
        val json = JSONObject(jsonStr)
        val dataObj = json.optJSONObject("data") ?: JSONObject()
        val mangaArray = dataObj.optJSONArray("data") ?: json.optJSONArray("data") ?: return MangasPage(emptyList(), false)

        val mangas = mutableListOf<SManga>()
        for (i in 0 until mangaArray.length()) {
            val item = mangaArray.getJSONObject(i)
            val manga = SManga.create()
            val slug = item.optString("slug")
            val id = item.optString("id")
            manga.url = "/manga/$slug||$id"
            manga.title = item.optString("title").trim()
            
            val coverObj = item.optJSONObject("cover")
            val rawCover = coverObj?.optString("original")?.ifEmpty { null }
                ?: coverObj?.optString("thumbnail")?.ifEmpty { null }
                ?: item.optString("cover")
            manga.thumbnail_url = fixUrl(rawCover)
            mangas.add(manga)
        }

        val metaObj = dataObj.optJSONObject("meta")
        val currentPage = metaObj?.optInt("current_page", 1) ?: 1
        val lastPage = metaObj?.optInt("last_page", 1) ?: 1
        val hasNextPage = currentPage < lastPage

        return MangasPage(mangas, hasNextPage)
    }

    // --- Latest Updates ---

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$apiBaseUrl/manga?sort=latest&page=$page", headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    // --- Search ---

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$apiBaseUrl/manga?search=$query&page=$page", headers)
    }

    override fun searchMangaParse(response: Response): MangasPage = popularMangaParse(response)

    // --- Manga Details ---

    override fun mangaDetailsRequest(manga: SManga): Request {
        val slug = manga.url.substringAfter("/manga/").substringBefore("||")
        return GET("$apiBaseUrl/manga/$slug", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val jsonStr = response.body?.string() ?: ""
        val item = JSONObject(jsonStr)
        val manga = SManga.create()
        
        val slug = item.optString("slug")
        val id = item.optString("id")
        manga.url = "/manga/$slug||$id"
        manga.title = item.optString("title").trim()
        manga.description = item.optString("description")
        
        val coverObj = item.optJSONObject("cover")
        val rawCover = coverObj?.optString("original")?.ifEmpty { null }
            ?: coverObj?.optString("thumbnail")?.ifEmpty { null }
            ?: item.optString("cover")
        manga.thumbnail_url = fixUrl(rawCover)

        val genresArr = item.optJSONArray("genres")
        if (genresArr != null) {
            val genresList = mutableListOf<String>()
            for (i in 0 until genresArr.length()) {
                val g = genresArr.getJSONObject(i)
                genresList.add(g.optString("name"))
            }
            manga.genre = genresList.joinToString(", ")
        }

        val statusStr = item.optString("status").lowercase()
        manga.status = when {
            statusStr.contains("ongoing") -> SManga.ONGOING
            statusStr.contains("completed") -> SManga.COMPLETED
            statusStr.contains("hiatus") -> SManga.ON_HIATUS
            statusStr.contains("cancelled") -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }

        manga.initialized = true
        return manga
    }

    // --- Chapter List ---

    override fun chapterListRequest(manga: SManga): Request {
        val id = if (manga.url.contains("||")) {
            manga.url.substringAfter("||")
        } else {
            manga.url.substringAfterLast("/")
        }
        return GET("$apiBaseUrl/manga/$id/chapters", headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val jsonStr = response.body?.string() ?: ""
        val jsonArray = try {
            org.json.JSONArray(jsonStr)
        } catch (e: Exception) {
            val obj = JSONObject(jsonStr)
            obj.optJSONArray("data") ?: org.json.JSONArray()
        }

        val chapters = mutableListOf<SChapter>()
        for (i in 0 until jsonArray.length()) {
            val ch = jsonArray.getJSONObject(i)
            val chapter = SChapter.create()
            val chId = ch.optString("id")
            val mangaId = ch.optString("mangaId")
            chapter.url = "/manga/$mangaId/chapters/$chId"
            
            val num = ch.optDouble("number", 0.0)
            val chNumStr = if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
            val chTitle = ch.optString("title").takeIf { it.isNotBlank() && it != "null" }
            
            chapter.name = "Chapter $chNumStr" + (if (chTitle != null) " - $chTitle" else "")
            chapter.date_upload = parseIsoDate(ch.optString("publishedAt"))
            chapters.add(chapter)
        }
        return chapters
    }

    // --- Page List ---

    override fun pageListRequest(chapter: SChapter): Request {
        return GET("$apiBaseUrl${chapter.url}", headers)
    }

    override fun pageListParse(response: Response): List<Page> {
        val jsonStr = response.body?.string() ?: ""
        val obj = JSONObject(jsonStr)
        val pagesArr = obj.optJSONArray("pages") ?: return emptyList()

        val pages = mutableListOf<Page>()
        for (i in 0 until pagesArr.length()) {
            val p = pagesArr.getJSONObject(i)
            val rawUrl = p.optString("url")
            val imgUrl = fixUrl(rawUrl)
            if (imgUrl.isNotEmpty()) {
                pages.add(Page(i, "", imgUrl))
            }
        }
        return pages
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("Not used")
    }

    private fun fixUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return url.replace("https://backend.ybxmanga.in", cdnHost)
            .replace("http://backend.ybxmanga.in", cdnHost)
    }

    private fun parseIsoDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
