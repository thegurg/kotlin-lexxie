package ru.radiationx.anilibria.screen.moviedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.domain.movie.Movie
import ru.radiationx.data.repository.MovieRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class MovieDetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val tmdbId: Int,
) : LifecycleViewModel() {

    val movieData = MutableStateFlow<Movie?>(null)
    val backdropUrl = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    init {
        loadMovieDetails()
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            coRunCatching {
                movieRepository.getMovieDetails(tmdbId)
            }.onSuccess { movie ->
                movieData.value = movie
                backdropUrl.value = movie.backdropUrl
            }.onFailure { e ->
                Timber.e(e, "Failed to load movie details")
                error.value = e.message
            }
            isLoading.value = false
        }
    }

    fun onPlayClick() {
        movieData.value?.let { movie ->
            // TODO: Navigate to player with movie
        }
    }

    fun onFavoriteClick() {
        // TODO: Add to favorites
    }
}
