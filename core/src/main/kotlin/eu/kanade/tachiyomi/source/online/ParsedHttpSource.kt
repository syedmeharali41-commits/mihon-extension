package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    open fun popularMangaSelector(): String = ""
    open fun popularMangaFromElement(element: Element): SManga = throw UnsupportedOperationException("Not implemented")
    open fun popularMangaNextPageSelector(): String? = null

    open fun latestUpdatesSelector(): String = popularMangaSelector()
    open fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)
    open fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()

    open fun searchMangaSelector(): String = popularMangaSelector()
    open fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    open fun searchMangaNextPageSelector(): String? = popularMangaNextPageSelector()

    open fun mangaDetailsParse(document: Document): SManga = throw UnsupportedOperationException("Not implemented")
    open fun chapterListSelector(): String = ""
    open fun chapterFromElement(element: Element): SChapter = throw UnsupportedOperationException("Not implemented")
    open fun pageListParse(document: Document): List<Page> = throw UnsupportedOperationException("Not implemented")
    open fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not implemented")
}
