package ru.radiationx.lexxie.screen.player.content

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.GuidedAction
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.lexxie.common.fragment.FakeGuidedStepFragment
import ru.radiationx.lexxie.common.fragment.GuidedRouter
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.quill.get
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.subscribeTo
import timber.log.Timber
import javax.inject.Inject

class ContentEpisodesFragment : FakeGuidedStepFragment() {

    companion object {
        private const val ACTION_SEASON_PREFIX = 1000L
        private const val ACTION_EPISODE_PREFIX = 2000L

        fun newInstance(tmdbId: Int, seasonNumber: Int = 1, episodeNumber: Int = 1): ContentEpisodesFragment {
            return ContentEpisodesFragment().apply {
                arguments = Bundle().apply {
                    putInt("tmdb_id", tmdbId)
                    putInt("season", seasonNumber)
                    putInt("episode", episodeNumber)
                }
            }
        }
    }

    private val viewModel by viewModel<ContentEpisodesViewModel> { argExtra }

    @Inject
    lateinit var guidedRouter: GuidedRouter

    override fun onProvideTheme(): Int = ru.radiationx.lexxie.R.style.AppTheme_Player_LeanbackWizard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        subscribeTo(viewModel.tvSeriesData.filterNotNull()) { tvSeries ->
            Timber.d("tvSeriesData loaded: ${tvSeries.name}, seasons: ${tvSeries.seasons.size}")
            subscribeTo(viewModel.episodesMap) { episodesMap ->
                Timber.d("episodesMap updated: ${episodesMap.size} seasons with episodes")
                buildFlatSeasonList(tvSeries.seasons, episodesMap)
            }
        }
    }

    private fun buildFlatSeasonList(
        seasons: List<TvSeries.Season>,
        episodesMap: Map<Int, List<TvSeries.Episode>>
    ) {
        if (seasons.isEmpty()) {
            Timber.w("No seasons to show")
            return
        }

        val actions = mutableListOf<GuidedAction>()

        seasons
            .filter { it.seasonNumber > 0 }
            .forEach { season ->
                Timber.d("Building season: ${season.seasonNumber} - ${season.name}")

                // 1. Сезон как заголовок (НЕ кликабельный)
                actions.add(
                    GuidedAction.Builder(requireContext())
                        .id(ACTION_SEASON_PREFIX + season.seasonNumber)
                        .title(season.name ?: "Сезон ${season.seasonNumber}")
                        .description("${season.episodeCount} эпизодов")
                        .focusable(false)
                        .enabled(false)
                        .build()
                )
                Timber.d("Season ${season.seasonNumber} added as header")

                // 2. Эпизоды (кликабельные)
                val episodes = episodesMap[season.seasonNumber] ?: emptyList()
                Timber.d("Season ${season.seasonNumber} has ${episodes.size} episodes")

                episodes.forEach { episode ->
                    val description = buildString {
                        if (episode.airDate != null) {
                            append(episode.airDate)
                        }
                        if (episode.runtime != null) {
                            if (isNotEmpty()) append(" • ")
                            append("${episode.runtime} мин")
                        }
                    }

                    actions.add(
                        GuidedAction.Builder(requireContext())
                            .id(ACTION_EPISODE_PREFIX + episode.episodeNumber)
                            .title("   ${episode.episodeNumber}. ${episode.name}")
                            .description(description.takeIf { it.isNotEmpty() })
                            .focusable(true)
                            .enabled(true)
                            .build()
                    )
                }
            }

        Timber.d("Setting ${actions.size} total actions (seasons + episodes)")
        setActions(actions)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        Timber.d("Action clicked: ${action.id}, title: ${action.title}")

        when {
            action.id >= ACTION_EPISODE_PREFIX -> {
                val episodeNumber = (action.id - ACTION_EPISODE_PREFIX).toInt()
                val episode = findEpisodeByNumber(episodeNumber)
                if (episode != null) {
                    Timber.d("Episode clicked: ${episode.name}")
                    viewModel.onEpisodeClick(episode)
                }
            }
            action.id >= ACTION_SEASON_PREFIX -> {
                Timber.d("Season header clicked - ignoring (it's a header)")
            }
        }
    }

    private fun findEpisodeByNumber(episodeNumber: Int): TvSeries.Episode? {
        val episodesMap = viewModel.episodesMap.value
        for ((seasonNum, episodes) in episodesMap) {
            val episode = episodes.find { it.episodeNumber == episodeNumber }
            if (episode != null) {
                return episode
            }
        }
        return null
    }

    private val argExtra by lazy {
        ContentEpisodesExtra(
            tmdbId = arguments?.getInt("tmdb_id") ?: 0,
            seasonNumber = arguments?.getInt("season") ?: 1,
            episodeNumber = arguments?.getInt("episode") ?: 1
        )
    }
}
