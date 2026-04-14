package ru.radiationx.anilibria.screen.movies

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
import ru.radiationx.data.repository.MovieRepository
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Фильмы"

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
            searchMovies(searchQuery!!, requestPage)
        } else {
            getPopularMovies(requestPage)
        }
    }

    private suspend fun getPopularMovies(page: Int): List<LibriaCard> {
        return movieRepository
            .getPopularMovies(page)
            .data
            .map { converter.toCard(it) }
    }

    private suspend fun searchMovies(query: String, page: Int): List<LibriaCard> {
        return movieRepository
            .searchMovies(query, page)
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
            delay(500) // Debounce
            onRefreshClick()
        }
    }

    fun clearSearch() {
        searchQuery = null
        searchJob?.cancel()
        onRefreshClick()
    }
}
