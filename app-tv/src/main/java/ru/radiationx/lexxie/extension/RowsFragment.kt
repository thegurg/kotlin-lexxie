package ru.radiationx.lexxie.extension

import androidx.fragment.app.Fragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import ru.radiationx.lexxie.common.BaseCardsViewModel
import ru.radiationx.lexxie.common.CardDiffCallback
import ru.radiationx.lexxie.ui.presenter.CardPresenterSelector
import ru.radiationx.shared.ktx.android.subscribeTo

fun Fragment.createCardsRowBy(
    rowId: Long,
    rowsAdapter: ArrayObjectAdapter,
    viewModel: BaseCardsViewModel
): ListRow {
    val cardsPresenter = CardPresenterSelector {
        viewModel.onLinkCardBind()
    }
    val cardsAdapter = ArrayObjectAdapter(cardsPresenter)
    val row = ListRow(rowId, HeaderItem(viewModel.defaultTitle), cardsAdapter)
    subscribeTo(viewModel.cardsData) {
        cardsAdapter.setItems(it, CardDiffCallback)
    }
    subscribeTo(viewModel.rowTitle) {
        val position = rowsAdapter.indexOf(row)
        row.headerItem = HeaderItem(it)
        rowsAdapter.notifyArrayItemRangeChanged(position, 1)
    }
    return row
}