package eu.kanade.tachiyomi.extension.en.ybxmanga

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

class YbxManga : ParsedHttpSource() {

    override val name = "YBX Manga"

    override val baseUrl = "https://www.ybxmanga.in"

    override val lang = "en"

    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        .add("Accept-Language", "en-US,en;q=0.5")
        .add("Referer", "$baseUrl/")

    // --- Popular Manga ---

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/", headers)
    }

    override fun popularMangaSelector(): String = "a[href*='/manga/']"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        val href = element.attr("href")
        manga.setUrlWithoutDomain(href)

        val titleEl = element.selectFirst("p.font-bold, p.font-semibold, h3, h4, span.title, div.title")
        manga.title = titleEl?.text()?.ifEmpty { null }
            ?: element.attr("title").ifEmpty { null }
            ?: element.text().takeIf { it.isNotBlank() }
            ?: href.substringAfterLast("/").replace("-", " ").capitalizeWords()

        val img = element.selectFirst("img")
        manga.thumbnail_url = img?.attr("abs:src")?.takeIf { it.isNotEmpty() }
            ?: img?.attr("src")?.takeIf { it.isNotEmpty() }
            ?: img?.attr("abs:data-src")?.takeIf { it.isNotEmpty() }
            
        return manga
    }

    override fun popularMangaNextPageSelector(): String? = null

    // --- Latest Updates ---

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/", headers)
    }

    override fun latestUpdatesSelector(): String = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector(): String? = null

    // --- Search ---

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/", headers)
    }

    override fun searchMangaSelector(): String = popularMangaSelector()

    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector(): String? = null

    // --- Manga Details ---

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        
        manga.title = document.selectFirst("h1, .manga-title, title")?.text()?.replace(" | YBX Manga", "") ?: "YBX Manga"
        manga.author = document.select("span:contains(Author) + span, div:contains(Author)").text().ifEmpty { "YBX Manga" }
        manga.artist = document.select("span:contains(Artist) + span, div:contains(Artist)").text().ifEmpty { "YBX Manga" }
        manga.description = document.select("p.description, p.text-muted-foreground, p.leading-relaxed, meta[name='description']").text()
        manga.genre = document.select("a[href*='genre='], span.genre-badge").joinToString { it.text() }
        
        val statusText = document.text().lowercase()
        manga.status = when {
            statusText.contains("ongoing") -> SManga.ONGOING
            statusText.contains("completed") -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }

        manga.thumbnail_url = document.selectFirst("img[src*='cover'], img[src*='manga']")?.attr("abs:src")
        return manga
    }

    // --- Chapter List ---

    override fun chapterListSelector(): String = "a[href*='/chapter-'], a[href*='/read/'], a[href*='/manga/']"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.name = element.text().ifEmpty { "Chapter 1" }
        chapter.setUrlWithoutDomain(element.attr("href"))
        chapter.date_upload = System.currentTimeMillis()
        return chapter
    }

    // --- Page List ---

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        val imgElements = document.select("img[src*='chapter'], img[src*='page'], div.reader-container img, div.page-break img, .chapter-content img, main img")
        
        imgElements.forEach { element ->
            val imageUrl = element.attr("abs:src")
                .ifEmpty { element.attr("abs:data-src") }
                .ifEmpty { element.attr("abs:data-lazy-src") }
                .ifEmpty { element.attr("src") }
                .ifEmpty { element.attr("data-src") }
                
            if (imageUrl.isNotEmpty() && !imageUrl.contains("logo") && !imageUrl.contains("avatar") && !imageUrl.contains("banner") && !imageUrl.contains("icon")) {
                pages.add(Page(pages.size, "", imageUrl))
            }
        }
        return pages
    }

    override fun imageUrlParse(document: Document): String {
        throw UnsupportedOperationException("Not used")
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
