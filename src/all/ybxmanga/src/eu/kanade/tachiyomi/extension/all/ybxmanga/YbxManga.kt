package eu.kanade.tachiyomi.extension.all.ybxmanga

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

class YbxManga : ParsedHttpSource() {

    override val name = "YBX Manga"

    override val baseUrl = "https://www.ybxmanga.in"

    override val lang = "en"

    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .add("Referer", "$baseUrl/")

    // --- Popular Manga ---

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/browse?sort=popular&page=$page", headers)
    }

    override fun popularMangaSelector(): String = "a[href*='/manga/'], div.manga-card-hover a"

    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            val titleEl = element.selectFirst("p.font-bold, p.font-semibold, div.title")
            title = titleEl?.text() ?: element.attr("title")
            
            val urlAttr = element.attr("href")
            setUrlWithoutDomain(urlAttr)

            val img = element.selectFirst("img")
            thumbnail_url = img?.attr("abs:src") ?: img?.attr("src")
        }
    }

    override fun popularMangaNextPageSelector(): String? = "a[rel='next'], a.next-page"

    // --- Latest Updates ---

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/browse?sort=latest&page=$page", headers)
    }

    override fun latestUpdatesSelector(): String = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()

    // --- Search ---

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/browse?q=${query}&page=$page", headers)
    }

    override fun searchMangaSelector(): String = popularMangaSelector()

    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector(): String? = popularMangaNextPageSelector()

    // --- Manga Details ---

    override fun mangaDetailsParse(document: Document): SManga {
        return SManga.create().apply {
            title = document.selectFirst("h1, .manga-title")?.text() ?: "Unknown Title"
            author = document.select("span:contains(Author) + span, div:contains(Author)").text()
            artist = document.select("span:contains(Artist) + span, div:contains(Artist)").text()
            description = document.select("p.description, p.text-muted-foreground, p.leading-relaxed").text()
            genre = document.select("a[href*='genre='], span.genre-badge").joinToString { it.text() }
            
            val statusText = document.select("span:contains(Status) + span").text().lowercase()
            status = when {
                statusText.contains("ongoing") -> SManga.ONGOING
                statusText.contains("completed") -> SManga.COMPLETED
                else -> SManga.UNKNOWN
            }

            thumbnail_url = document.selectFirst("div.manga-cover img, img[alt*='cover']")?.attr("abs:src")
        }
    }

    // --- Chapters ---

    override fun chapterListSelector(): String = "a[href*='/chapter-'], a[href*='/read/'], div.chapter-item a"

    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            name = element.selectFirst("span.font-bold, span.chapter-title")?.text() ?: element.text()
            setUrlWithoutDomain(element.attr("href"))
            date_upload = parseDate(element.selectFirst("span.time, span.date")?.text())
        }
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNull_orEmpty()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()

    // --- Page List ---

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        val imgElements = document.select("div.reader-container img, img.page-image, main img[src*='chapter']")
        
        imgElements.forEachIndexed { index, element ->
            val imageUrl = element.attr("abs:src").ifEmpty { element.attr("abs:data-src") }
            if (imageUrl.isNotEmpty()) {
                pages.add(Page(index, "", imageUrl))
            }
        }
        
        return pages
    }

    override fun imageUrlParse(document: Document): String {
        throw UnsupportedOperationException("Not used")
    }
}
