package ru.radiationx.lexxie.screen.player.content

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.lexxie.common.fragment.GuidedRouter
import ru.radiationx.lexxie.screen.LifecycleViewModel
import ru.radiationx.lexxie.screen.ContentPlayerScreen
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.repository.TvSeriesRepository
import timber.log.Timber
import javax.inject.Inject

class ContentEpisodesViewModel @Inject constructor(
    private val argExtra: ContentEpisodesExtra,
    private val tvSeriesRepository: TvSeriesRepository,
    private val guidedRouter: GuidedRouter,
    private val router: com.github.terrakok.cicerone.Router,
) : LifecycleViewModel() {

    val tvSeriesData = MutableStateFlow<TvSeries?>(null)
    val episodesMap = MutableStateFlow<Map<Int, List<TvSeries.Episode>>>(emptyMap())
    val selectedSeason = MutableStateFlow(argExtra.seasonNumber)
    val isLoading = MutableStateFlow(true)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                Timber.d("Loading TV series data for tmdbId: ${argExtra.tmdbId}")
                val tvSeries = tvSeriesRepository.getTvDetails(argExtra.tmdbId)
                tvSeriesData.value = tvSeries
                Timber.d("Loaded ${tvSeries.seasons.size} seasons")
                
                loadAllSeasons(tvSeries.seasons)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load TV series")
                tvSeriesData.value = null
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun loadAllSeasons(seasons: List<TvSeries.Season>) {
        viewModelScope.launch {
            val episodesMapResult = mutableMapOf<Int, List<TvSeries.Episode>>()
            
            seasons
                .filter { it.seasonNumber > 0 }
                .forEach { season ->
                    try {
                        Timber.d("Loading episodes for season ${season.seasonNumber}")
                        // Вызываем getSeasonDetails для каждого сезона чтобы получить эпизоды
                        val seasonData = tvSeriesRepository.getSeasonDetails(argExtra.tmdbId, season.seasonNumber)
                        
                        episodesMapResult[season.seasonNumber] = seasonData.episodes
                        Timber.d("Season ${season.seasonNumber}: ${seasonData.episodes.size} episodes")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load season ${season.seasonNumber}")
                        episodesMapResult[season.seasonNumber] = emptyList()
                    }
                }
            
            episodesMap.value = episodesMapResult
            Timber.d("All seasons loaded: ${episodesMapResult.size} seasons")
        }
    }

    fun selectSeason(seasonNumber: Int) {
        Timber.d("selectSeason called: $seasonNumber")
        selectedSeason.value = seasonNumber
    }

    fun onEpisodeClick(episode: TvSeries.Episode) {
        Timber.d("onEpisodeClick: ${episode.name}")
        guidedRouter.close()
        router.navigateTo(ContentPlayerScreen(
            tmdbId = argExtra.tmdbId,
            isMovie = false,
            season = episode.seasonNumber,
            episode = episode.episodeNumber
        ))
    }
}
