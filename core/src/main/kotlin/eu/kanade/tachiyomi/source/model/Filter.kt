package eu.kanade.tachiyomi.source.model

sealed class Filter<T>(val name: String, val state: T)
class FilterList(val list: List<Filter<*>>) : List<Filter<*>> by list {
    constructor(vararg filters: Filter<*>) : this(filters.toList())
}
