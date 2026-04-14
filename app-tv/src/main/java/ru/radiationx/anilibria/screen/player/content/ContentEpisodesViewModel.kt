package ru.radiationx.anilibria.screen.player.content

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.ContentPlayerScreen
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.repository.TvSeriesRepository
import javax.inject.Inject

class ContentEpisodesViewModel @Inject constructor(
    private val argExtra: ContentEpisodesExtra,
    private val tvSeriesRepository: TvSeriesRepository,
    private val guidedRouter: GuidedRouter,
    private val router: com.github.terrakok.cicerone.Router,
) : LifecycleViewModel() {

    val tvSeriesData = MutableStateFlow<TvSeries?>(null)
    val episodesData = MutableStateFlow<List<TvSeries.Episode>>(emptyList())
    val selectedSeason = MutableStateFlow(argExtra.seasonNumber)
    val isLoading = MutableStateFlow(true)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val tvSeries = tvSeriesRepository.getTvDetails(argExtra.tmdbId)
                tvSeriesData.value = tvSeries
                loadEpisodes(tvSeries.seasons, selectedSeason.value)
            } catch (e: Exception) {
                tvSeriesData.value = null
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun loadEpisodes(seasons: List<TvSeries.Season>, seasonNumber: Int) {
        viewModelScope.launch {
            try {
                val season = seasons.find { it.seasonNumber == seasonNumber }
                    ?: tvSeriesRepository.getSeasonDetails(argExtra.tmdbId, seasonNumber)
                
                episodesData.value = season.episodes
            } catch (e: Exception) {
                episodesData.value = emptyList()
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        selectedSeason.value = seasonNumber
        tvSeriesData.value?.seasons?.let { seasons ->
            loadEpisodes(seasons, seasonNumber)
        }
    }

    fun onEpisodeClick(episode: TvSeries.Episode) {
        guidedRouter.close()
        router.navigateTo(ContentPlayerScreen(
            tmdbId = argExtra.tmdbId,
            isMovie = false,
            season = episode.seasonNumber,
            episode = episode.episodeNumber
        ))
    }
}
