package ru.radiationx.lexxie.screen.search.year

import ru.radiationx.lexxie.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.lexxie.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.viewModel

class SearchYearGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by viewModel<SearchYearViewModel> {
        argExtra
    }
}