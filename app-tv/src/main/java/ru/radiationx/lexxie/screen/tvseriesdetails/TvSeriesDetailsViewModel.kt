package ru.radiationx.lexxie.screen.tvseriesdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.lexxie.screen.LifecycleViewModel
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.repository.TvSeriesRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import javax.inject.Inject

class TvSeriesDetailsViewModel @Inject constructor(
    private val tvSeriesRepository: TvSeriesRepository,
    private val tmdbId: Int,
) : LifecycleViewModel() {

    val tvSeriesData = MutableStateFlow<TvSeries?>(null)
    val backdropUrl = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    init {
        loadTvSeriesDetails()
    }

    private fun loadTvSeriesDetails() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            coRunCatching {
                tvSeriesRepository.getTvDetails(tmdbId)
            }.onSuccess { tvSeries ->
                tvSeriesData.value = tvSeries
                backdropUrl.value = tvSeries.backdropUrl
            }.onFailure { e ->
                Timber.e(e, "Failed to load TV series details")
                error.value = e.message
            }
            isLoading.value = false
        }
    }

    fun onPlayClick() {
        tvSeriesData.value?.let { tvSeries ->
            // TODO: Navigate to season/episode selection, then to player
        }
    }

    fun onFavoriteClick() {
        // TODO: Add to favorites
    }
}
