package ru.radiationx.lexxie.screen.search.season

import ru.radiationx.lexxie.screen.search.BaseSearchValuesGuidedFragment
import ru.radiationx.lexxie.screen.search.BaseSearchValuesViewModel
import ru.radiationx.quill.viewModel

class SearchSeasonGuidedFragment : BaseSearchValuesGuidedFragment() {

    override val viewModel: BaseSearchValuesViewModel by viewModel<SearchSeasonViewModel> {
        argExtra
    }
}