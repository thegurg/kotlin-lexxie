package ru.radiationx.anilibria.screen.search.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridPresenter
import ru.radiationx.anilibria.common.CardDiffCallback
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.fragment.BaseVerticalGridFragment
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo

class ContentSearchFragment : BaseVerticalGridFragment() {

    companion object {
        private const val ARG_TYPE = "type"

        fun newInstance(type: ContentSearchType): ContentSearchFragment {
            return ContentSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                }
            }
        }
    }

    private lateinit var searchType: ContentSearchType

    private val cardsPresenter = CardPresenterSelector {
        cardsViewModel.onLinkCardBind()
    }
    private val cardsAdapter = ArrayObjectAdapter(cardsPresenter)

    private val backgroundManager by inject<GradientBackgroundManager>()
    private val cardsViewModel by viewModel<ContentSearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchType = ContentSearchType.valueOf(
            arguments?.getString(ARG_TYPE) ?: ContentSearchType.MOVIE.name
        )
        cardsViewModel.init(searchType)
        title = cardsViewModel.defaultTitle
        setGridPresenter(VerticalGridPresenter().apply {
            numberOfColumns = 6
        })
    }

    override fun onInflateTitleView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(
            ru.radiationx.anilibria.R.layout.lb_search_titleview,
            parent,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(cardsViewModel)

        setOnSearchClickedListener {
            showSearchInput()
        }

        backgroundManager.clearGradient()
        setOnItemViewSelectedListener { _, item, _, _ ->
            backgroundManager.applyCard(item)
            when (item) {
                is LibriaCard -> setDescription(item.title, item.description)
                is LinkCard -> setDescription(item.title, "")
                is LoadingCard -> setDescription(item.title, item.description)
                else -> setDescription("", "")
            }
        }

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is LinkCard -> cardsViewModel.onLinkCardClick()
                is LoadingCard -> cardsViewModel.onLoadingCardClick()
                is LibriaCard -> cardsViewModel.onLibriaCardClick(item)
            }
        }

        prepareEntranceTransition()
        adapter = cardsAdapter

        progressBarManager.initialDelay = 0

        subscribeTo(cardsViewModel.cardsData) {
            cardsAdapter.setItems(it, CardDiffCallback)
            startEntranceTransition()
        }
    }

    private fun showSearchInput() {
        val input = EditText(requireContext()).apply {
            hint = when (searchType) {
                ContentSearchType.MOVIE -> "Введите название фильма..."
                ContentSearchType.TV_SERIES -> "Введите название сериала..."
            }
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    cardsViewModel.search(text.toString())
                    true
                } else {
                    false
                }
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(cardsViewModel.defaultTitle)
            .setView(input)
            .setPositiveButton("Поиск") { _, _ ->
                cardsViewModel.search(input.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
