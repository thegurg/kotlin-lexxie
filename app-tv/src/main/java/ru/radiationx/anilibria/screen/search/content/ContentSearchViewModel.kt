package ru.radiationx.anilibria.screen.search.content

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.BaseCardsViewModel
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.data.repository.MovieRepository
import ru.radiationx.data.repository.TvSeriesRepository
import javax.inject.Inject

class ContentSearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val tvSeriesRepository: TvSeriesRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Поиск"

    private lateinit var searchType: ContentSearchType
    private var currentQuery: String = ""

    private var searchJob: Job? = null

    fun init(type: ContentSearchType) {
        searchType = type
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> {
        if (currentQuery.isBlank()) {
            return emptyList()
        }
        return when (searchType) {
            ContentSearchType.MOVIE -> searchMovies(currentQuery, requestPage)
            ContentSearchType.TV_SERIES -> searchTvSeries(currentQuery, requestPage)
        }
    }

    private suspend fun searchMovies(query: String, page: Int): List<LibriaCard> {
        return movieRepository
            .searchMovies(query, page)
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
        currentQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            onRefreshClick()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            onRefreshClick()
        }
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        super.onLibriaCardClick(card)
        cardRouter.navigate(card)
    }
}
