package ru.radiationx.lexxie.screen.search.genre

import ru.radiationx.lexxie.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.lexxie.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.viewModel

class SearchGenreGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by viewModel<SearchGenreViewModel> {
        argExtra
    }
}