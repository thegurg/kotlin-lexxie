package ru.radiationx.lexxie.screen.moviedetails

import android.os.Bundle
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import ru.radiationx.lexxie.common.GradientBackgroundManager
import ru.radiationx.lexxie.common.LibriaDetailsRow
import ru.radiationx.lexxie.common.RowDiffCallback
import ru.radiationx.lexxie.ui.presenter.MovieDetailsPresenter
import ru.radiationx.lexxie.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.lexxie.ui.presenter.cust.CustomListRowViewHolder
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

class MovieDetailsFragment : RowsSupportFragment() {

    companion object {
        private const val ARG_TMDB_ID = "tmdb_id"

        const val MOVIE_ROW_ID = 1L

        fun newInstance(tmdbId: Int) = MovieDetailsFragment().putExtra {
            putInt(ARG_TMDB_ID, tmdbId)
        }
    }

    private val tmdbId: Int by lazy { getExtraNotNull(ARG_TMDB_ID) }

    private val backgroundManager by inject<GradientBackgroundManager>()

    private val argExtra by lazy { MovieDetailsExtra(tmdbId) }

    private val headerViewModel by viewModel<MovieDetailsHeaderViewModel> { argExtra }

    private val rowsPresenter by lazy {
        ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(
                LibriaDetailsRow::class.java, MovieDetailsPresenter(
                    playClickListener = headerViewModel::onPlayClick,
                    favoriteClickListener = headerViewModel::onFavoriteClick
                )
            )
        }
    }

    private val rowsAdapter by lazy { ArrayObjectAdapter(rowsPresenter) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(headerViewModel)

        adapter = rowsAdapter

        setOnItemViewSelectedListener { _, item, rowViewHolder, row ->
            if (row is LibriaDetailsRow) {
                applyImage(row.details?.image.orEmpty())
            }
            if (rowViewHolder is CustomListRowViewHolder) {
                when (item) {
                    is LibriaDetailsRow -> {
                        item.details?.let {
                            rowViewHolder.setDescription(it.titleRu, it.extra)
                        }
                    }
                    else -> {
                        rowViewHolder.setDescription("", "")
                    }
                }
            }
        }

        val rowMap = mutableMapOf<Long, Row>()
        val rows = listOf(MOVIE_ROW_ID).map { rowId ->
            val row = rowMap.getOrPut(rowId) { createHeaderRowBy(rowId) }
            row
        }
        rowsAdapter.setItems(rows, RowDiffCallback)
    }

    private fun createHeaderRowBy(rowId: Long): Row {
        val row = LibriaDetailsRow(rowId)
        subscribeTo(headerViewModel.releaseData) { details ->
            val position = rowsAdapter.indexOf(row)
            if (position >= 0) {
                row.details = details
                rowsAdapter.notifyArrayItemRangeChanged(position, 1)
            }
        }
        subscribeTo(headerViewModel.progressState) { state ->
            val position = rowsAdapter.indexOf(row)
            if (position >= 0) {
                row.state = state
                rowsAdapter.notifyArrayItemRangeChanged(position, 1)
            }
        }
        return row
    }

    private fun applyImage(image: String) {
        backgroundManager.applyImage(image, colorSelector = { null }) {
            val hslColor = FloatArray(3)
            ColorUtils.colorToHSL(it, hslColor)
            hslColor[1] = (hslColor[1] + 0.05f).coerceAtMost(1.0f)
            hslColor[2] = (hslColor[2] + 0.05f).coerceAtMost(1.0f)
            ColorUtils.HSLToColor(hslColor)
        }
    }
}
