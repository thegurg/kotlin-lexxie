package ru.radiationx.lexxie.ui.presenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.RowPresenter
import ru.radiationx.lexxie.R
import ru.radiationx.lexxie.common.LibriaDetailsRow
import ru.radiationx.lexxie.databinding.RowDetailMovieBinding
import ru.radiationx.shared_app.imageloader.showImageUrl

class TvSeriesDetailsPresenter(
    private val playClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
) : RowPresenter() {

    init {
        headerPresenter = null
    }

    override fun isUsingDefaultSelectEffect(): Boolean {
        return false
    }

    override fun createRowViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_detail_movie, parent, false)
        return TvSeriesDetailsViewHolder(view, playClickListener, favoriteClickListener)
    }

    override fun onBindRowViewHolder(vh: ViewHolder, item: Any) {
        super.onBindRowViewHolder(vh, item)
        vh as TvSeriesDetailsViewHolder
        item as LibriaDetailsRow
        vh.bind(item)
    }
}

class TvSeriesDetailsViewHolder(
    itemView: View,
    private val playClickListener: () -> Unit,
    private val favoriteClickListener: () -> Unit,
) : RowPresenter.ViewHolder(itemView) {

    private val binding by lazy { RowDetailMovieBinding.bind(view) }

    init {
        binding.movieActionPlay.setOnClickListener { playClickListener.invoke() }
        binding.movieActionPlay.text = "Сезоны"
        binding.movieActionFavorite.setOnClickListener { favoriteClickListener.invoke() }
    }

    fun bind(row: LibriaDetailsRow) {
        val details = row.details ?: return

        binding.movieTitle.text = details.titleRu
        binding.movieSubtitle.text = details.extra
        binding.movieDescription.text = details.description

        binding.movieActionFavorite.text = if (details.isFavorite) {
            "Убрать из избранного"
        } else {
            "В избранное"
        }

        binding.moviePoster.showImageUrl(details.image)
    }
}
