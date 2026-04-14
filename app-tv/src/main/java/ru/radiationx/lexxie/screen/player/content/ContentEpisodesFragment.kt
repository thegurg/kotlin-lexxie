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
            buildSeasonActions(tvSeries.seasons)
        }

        subscribeTo(viewModel.episodesData) { episodes ->
            buildEpisodeActions(episodes)
        }
    }

    private fun buildSeasonActions(seasons: List<TvSeries.Season>) {
        if (seasons.size <= 1) {
            return
        }

        val seasonActions = seasons
            .filter { it.seasonNumber > 0 }
            .map { season ->
                GuidedAction.Builder(requireContext())
                    .id(ACTION_SEASON_PREFIX + season.seasonNumber)
                    .title(season.name ?: "Сезон ${season.seasonNumber}")
                    .description("${season.episodeCount} эпизодов")
                    .focusable(true)
                    .enabled(true)
                    .build()
            }

        setActions(seasonActions)
    }

    private fun buildEpisodeActions(episodes: List<TvSeries.Episode>) {
        val episodeActions = episodes.map { episode ->
            val description = buildString {
                if (episode.airDate != null) {
                    append(episode.airDate)
                }
                if (episode.runtime != null) {
                    if (isNotEmpty()) append(" • ")
                    append("${episode.runtime} мин")
                }
            }
            GuidedAction.Builder(requireContext())
                .id(ACTION_EPISODE_PREFIX + episode.episodeNumber)
                .title(episode.name)
                .description(description.takeIf { it.isNotEmpty() })
                .focusable(true)
                .enabled(true)
                .build()
        }
        val existingSeasonActions = actions.filter { it.id < ACTION_EPISODE_PREFIX }
        setActions(existingSeasonActions + episodeActions)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when {
            action.id >= ACTION_EPISODE_PREFIX -> {
                val episodeNumber = (action.id - ACTION_EPISODE_PREFIX).toInt()
                val episode = viewModel.episodesData.value.find { it.episodeNumber == episodeNumber }
                if (episode != null) {
                    viewModel.onEpisodeClick(episode)
                }
            }
            action.id >= ACTION_SEASON_PREFIX -> {
                val seasonNumber = (action.id - ACTION_SEASON_PREFIX).toInt()
                viewModel.selectSeason(seasonNumber)
            }
        }
    }

    override fun onSubGuidedActionClicked(action: GuidedAction): Boolean {
        onGuidedActionClicked(action)
        return true
    }

    private val argExtra by lazy {
        ContentEpisodesExtra(
            tmdbId = arguments?.getInt("tmdb_id") ?: 0,
            seasonNumber = arguments?.getInt("season") ?: 1,
            episodeNumber = arguments?.getInt("episode") ?: 1
        )
    }
}
