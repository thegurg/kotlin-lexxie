package ru.radiationx.lexxie.di

import ru.radiationx.lexxie.screen.search.SearchController
import ru.radiationx.quill.QuillModule

class SearchModule : QuillModule() {

    init {
        single<SearchController>()
    }
}