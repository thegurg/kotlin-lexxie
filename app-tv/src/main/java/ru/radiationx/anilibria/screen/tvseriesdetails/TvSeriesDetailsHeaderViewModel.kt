package ru.radiationx.anilibria.screen.tvseriesdetails

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.DetailsState
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.TvSeriesDetailDataConverter
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.ContentEpisodesScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.repository.TvSeriesRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class TvSeriesDetailsHeaderViewModel @Inject constructor(
    argExtra: TvSeriesDetailsExtra,
    private val tvSeriesRepository: TvSeriesRepository,
    private val converter: TvSeriesDetailDataConverter,
    private val router: Router,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    private val tmdbId = argExtra.tmdbId

    val releaseData = MutableStateFlow<LibriaDetails?>(null)
    val progressState = MutableStateFlow(DetailsState(loadingProgress = true))

    init {
        loadTvSeriesDetails()
    }

    private fun loadTvSeriesDetails() {
        viewModelScope.launch {
            progressState.value = DetailsState(loadingProgress = true)
            coRunCatching {
                tvSeriesRepository.getTvDetails(tmdbId)
            }.onSuccess { tvSeries ->
                releaseData.value = converter.toDetail(tvSeries)
                progressState.value = DetailsState(loadingProgress = false)
            }.onFailure { e ->
                Timber.e(e, "Failed to load TV series details")
                progressState.value = DetailsState(loadingProgress = false)
            }
        }
    }

    fun onPlayClick() {
        guidedRouter.navigateTo(ContentEpisodesScreen(tmdbId))
    }

    fun onFavoriteClick() {
        // Add/remove from favorites
    }

    fun onDescriptionClick() {
        // Show full description
    }
}
