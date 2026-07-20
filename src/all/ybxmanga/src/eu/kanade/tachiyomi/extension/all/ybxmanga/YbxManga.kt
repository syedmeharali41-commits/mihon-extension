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
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
        .add("Referer", "$baseUrl/")

    // --- Popular Manga ---

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/browse?sort=popular&page=$page", headers)
    }

    override fun popularMangaSelector(): String = "div.manga-grid > div, a[href*='/manga/']"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        val link = if (element.tagName() == "a") element else element.selectFirst("a[href*='/manga/']") ?: element
        
        manga.setUrlWithoutDomain(link.attr("href"))
        
        val titleEl = element.selectFirst("p.font-bold, p.font-semibold, h3, div.title")
        manga.title = titleEl?.text() ?: link.attr("title").ifEmpty { "Manga" }

        val img = element.selectFirst("img")
        manga.thumbnail_url = img?.attr("abs:src")?.takeIf { it.isNotEmpty() }
            ?: img?.attr("src")?.takeIf { it.isNotEmpty() }
            ?: img?.attr("data-src")
            
        return manga
    }

    override fun popularMangaNextPageSelector(): String? = "a[rel='next'], button:contains(Next), a.next"

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
        val manga = SManga.create()
        
        manga.title = document.selectFirst("h1, .manga-title")?.text() ?: "Unknown Title"
        manga.author = document.select("span:contains(Author) + span, div:contains(Author)").text()
        manga.artist = document.select("span:contains(Artist) + span, div:contains(Artist)").text()
        manga.description = document.select("p.description, p.text-muted-foreground, p.leading-relaxed").text()
        manga.genre = document.select("a[href*='genre='], span.genre-badge").joinToString { it.text() }
        
        val statusText = document.select("span:contains(Status) + span, div.status").text().lowercase()
        manga.status = when {
            statusText.contains("ongoing") -> SManga.ONGOING
            statusText.contains("completed") -> SManga.COMPLETED
            statusText.contains("hiatus") -> SManga.ON_HIATUS
            statusText.contains("cancelled") -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }

        manga.thumbnail_url = document.selectFirst("div.manga-cover img, img[alt*='cover']")?.attr("abs:src")
        return manga
    }

    // --- Chapter List ---

    override fun chapterListSelector(): String = "a[href*='/chapter-'], a[href*='/read/'], div.chapter-list a"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        val titleEl = element.selectFirst("span.font-bold, span.chapter-title")
        chapter.name = titleEl?.text() ?: element.text()
        chapter.setUrlWithoutDomain(element.attr("href"))
        
        val timeStr = element.selectFirst("span.time, span.date, time")?.text()
        chapter.date_upload = parseDate(timeStr)
        return chapter
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

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
