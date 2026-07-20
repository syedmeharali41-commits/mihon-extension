package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    abstract fun popularMangaSelector(): String
    abstract fun popularMangaFromElement(element: Element): SManga
    abstract fun popularMangaNextPageSelector(): String?

    abstract fun latestUpdatesSelector(): String
    abstract fun latestUpdatesFromElement(element: Element): SManga
    abstract fun latestUpdatesNextPageSelector(): String?

    abstract fun searchMangaSelector(): String
    abstract fun searchMangaFromElement(element: Element): SManga
    abstract fun searchMangaNextPageSelector(): String?

    abstract fun mangaDetailsParse(document: Document): SManga
    abstract fun chapterListSelector(): String
    abstract fun chapterFromElement(element: Element): SChapter
    abstract fun pageListParse(document: Document): List<Page>
    abstract fun imageUrlParse(document: Document): String
}
