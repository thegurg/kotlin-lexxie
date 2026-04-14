package ru.radiationx.anilibria.screen.tvseries

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.TvSeriesRepository
import javax.inject.Inject

class TvSeriesViewModel @Inject constructor(
    private val tvSeriesRepository: TvSeriesRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Сериалы"

    override val preventClearOnRefresh: Boolean = true

    private var searchQuery: String? = null
    private var searchJob: Job? = null

    private var searchCallback: (() -> Unit)? = null

    companion object {
        val searchCard = LinkCard("Поиск")
    }

    fun setSearchCallback(callback: () -> Unit) {
        searchCallback = callback
    }

    override fun onLinkCardClick() {
        if (searchCallback != null) {
            searchCallback?.invoke()
        } else {
            super.onLinkCardClick()
        }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        cardRouter.navigate(card)
    }

    override fun onResume() {
        super.onResume()
        if (searchQuery.isNullOrEmpty()) {
            onRefreshClick()
        }
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        return if (!searchQuery.isNullOrEmpty()) {
            searchTvSeries(searchQuery!!, requestPage)
        } else {
            getPopularTvSeries(requestPage)
        }
    }

    private suspend fun getPopularTvSeries(page: Int): List<LibriaCard> {
        return tvSeriesRepository
            .getPopularTv(page)
            .data
            .map { converter.toCard(it) }
    }

    private suspend fun searchTvSeries(query: String, page: Int): List<LibriaCard> {
        return tvSeriesRepository
            .searchTv(query, page)
            .data
            .map { converter.toCard(it) }
    }

    fun search(query: String) {
        searchQuery = query.takeIf { it.isNotBlank() }
        searchJob?.cancel()
        if (searchQuery.isNullOrBlank()) {
            onRefreshClick()
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            onRefreshClick()
        }
    }

    fun clearSearch() {
        searchQuery = null
        searchJob?.cancel()
        onRefreshClick()
    }
}
