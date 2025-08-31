package app.wetube.manage

import android.content.SearchRecentSuggestionsProvider

class SearchManager : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "app.wetube.SearchManager"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}