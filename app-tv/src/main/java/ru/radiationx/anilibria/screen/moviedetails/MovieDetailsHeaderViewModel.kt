package ru.radiationx.anilibria.screen.moviedetails

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.MovieDetailDataConverter
import ru.radiationx.anilibria.screen.ContentPlayerScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.MovieRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class MovieDetailsHeaderViewModel @Inject constructor(
    argExtra: MovieDetailsExtra,
    private val movieRepository: MovieRepository,
    private val converter: MovieDetailDataConverter,
    private val router: Router,
) : LifecycleViewModel() {

    private val tmdbId = argExtra.tmdbId

    val releaseData = MutableStateFlow<LibriaDetails?>(null)
    val progressState = MutableStateFlow(DetailsState(loadingProgress = true))

    init {
        loadMovieDetails()
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            progressState.value = DetailsState(loadingProgress = true)
            coRunCatching {
                movieRepository.getMovieDetails(tmdbId)
            }.onSuccess { movie ->
                releaseData.value = converter.toDetail(movie)
                progressState.value = DetailsState(loadingProgress = false)
            }.onFailure { e ->
                Timber.e(e, "Failed to load movie details")
                progressState.value = DetailsState(loadingProgress = false)
            }
        }
    }

    fun onPlayClick() {
        router.navigateTo(ContentPlayerScreen(tmdbId, isMovie = true))
    }

    fun onFavoriteClick() {
        // Add/remove from favorites
    }

    fun onDescriptionClick() {
        // Show full description
    }
}
