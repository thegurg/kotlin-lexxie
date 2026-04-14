package ru.radiationx.anilibria.screen.tvseries

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardDiffCallback
import ru.radiationx.anilibria.common.GradientBackgroundManager
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.common.RowDiffCallback
import ru.radiationx.anilibria.extension.applyCard
import ru.radiationx.anilibria.screen.TvSeriesSearchScreen
import ru.radiationx.anilibria.ui.presenter.CardPresenterSelector
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.quillParentViewModel

class TvSeriesFragment : RowsSupportFragment() {

    private val rowsPresenter by lazy { CustomListRowPresenter() }
    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    private val backgroundManager by inject<GradientBackgroundManager>()
    private val router by inject<Router>()
    private val viewModel by quillParentViewModel<TvSeriesViewModel>()

    private val cardsPresenter = CardPresenterSelector {
        viewModel.onLinkCardBind()
    }
    private val cardsAdapter = ArrayObjectAdapter(cardsPresenter)

    private var searchDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        adapter = rowsAdapter
        onItemViewSelectedListener = ItemViewSelectedListener()

        viewModel.setSearchCallback { showSearchDialog() }

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is LinkCard -> {
                    if (item == TvSeriesViewModel.searchCard) {
                        showSearchDialog()
                    } else {
                        viewModel.onLinkCardBind()
                    }
                }
                is LoadingCard -> viewModel.onLoadingCardClick()
                is LibriaCard -> viewModel.onLibriaCardClick(item)
            }
        }

        val row = ListRow(1L, HeaderItem(viewModel.defaultTitle), cardsAdapter)

        subscribeTo(viewModel.cardsData) { cards ->
            val allCards = listOf(TvSeriesViewModel.searchCard) + cards
            cardsAdapter.setItems(allCards, CardDiffCallback)
        }

        rowsAdapter.add(row)
    }

    override fun onResume() {
        super.onResume()
        notifyReady()
    }

    override fun onPause() {
        super.onPause()
        searchDialog?.dismiss()
    }

    private fun showSearchDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Введите название сериала..."
            setSingleLine()
        }
        searchDialog = AlertDialog.Builder(requireContext())
            .setTitle("Поиск сериалов")
            .setView(editText)
            .setPositiveButton("Поиск") { _, _ ->
                viewModel.search(editText.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Открыть экран поиска") { _, _ ->
                router.navigateTo(TvSeriesSearchScreen())
            }
            .setOnDismissListener { searchDialog = null }
            .show()
    }

    private fun notifyReady() {
        mainFragmentAdapter.fragmentHost.notifyDataReady(mainFragmentAdapter)
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder, row: Row,
        ) {
            if (rowViewHolder is CustomListRowViewHolder) {
                backgroundManager.applyCard(item)
                when (item) {
                    is LibriaCard -> rowViewHolder.setDescription(item.title, item.description)
                    is LinkCard -> rowViewHolder.setDescription(item.title, "")
                    is LoadingCard -> rowViewHolder.setDescription(item.title, item.description)
                    else -> rowViewHolder.setDescription("", "")
                }
            }
        }
    }
}
