package ru.radiationx.lexxie.screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import ru.radiationx.lexxie.common.fragment.FakeGuidedStepFragment
import ru.radiationx.lexxie.common.fragment.GuidedAppScreen
import ru.radiationx.lexxie.screen.auth.credentials.AuthCredentialsGuidedFragment
import ru.radiationx.lexxie.screen.auth.main.AuthGuidedFragment
import ru.radiationx.lexxie.screen.auth.otp.AuthOtpGuidedFragment
import ru.radiationx.lexxie.screen.config.ConfigFragment
import ru.radiationx.lexxie.screen.details.DetailFragment
import ru.radiationx.lexxie.screen.details.other.DetailOtherGuidedFragment
import ru.radiationx.lexxie.screen.mainpages.MainPagesFragment
import ru.radiationx.lexxie.screen.player.PlayerFragment
import ru.radiationx.lexxie.screen.player.end_episode.EndEpisodeGuidedFragment
import ru.radiationx.lexxie.screen.player.end_season.EndSeasonGuidedFragment
import ru.radiationx.lexxie.screen.player.episodes.PlayerEpisodesGuidedFragment
import ru.radiationx.lexxie.screen.player.putIds
import ru.radiationx.lexxie.screen.player.quality.PlayerQualityGuidedFragment
import ru.radiationx.lexxie.screen.player.speed.PlayerSpeedGuidedFragment
import ru.radiationx.lexxie.screen.schedule.ScheduleFragment
import ru.radiationx.lexxie.screen.search.SearchFragment
import ru.radiationx.lexxie.screen.search.completed.SearchCompletedGuidedFragment
import ru.radiationx.lexxie.screen.search.genre.SearchGenreGuidedFragment
import ru.radiationx.lexxie.screen.search.putValues
import ru.radiationx.lexxie.screen.search.season.SearchSeasonGuidedFragment
import ru.radiationx.lexxie.screen.search.sort.SearchSortGuidedFragment
import ru.radiationx.lexxie.screen.search.year.SearchYearGuidedFragment
import ru.radiationx.lexxie.screen.suggestions.SuggestionsFragment
import ru.radiationx.lexxie.screen.trash.TestFragment
import ru.radiationx.lexxie.screen.search.content.ContentSearchFragment
import ru.radiationx.lexxie.screen.search.content.ContentSearchType
import ru.radiationx.lexxie.screen.player.content.ContentPlayerFragment
import ru.radiationx.lexxie.screen.player.content.ContentEpisodesFragment
import ru.radiationx.lexxie.screen.update.UpdateFragment
import ru.radiationx.lexxie.screen.update.source.UpdateSourceGuidedFragment
import ru.radiationx.data.entity.domain.search.SearchForm
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import com.github.terrakok.cicerone.androidx.FragmentScreen

class ConfigScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ConfigFragment()
    }
}

class MainPagesScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return MainPagesFragment()
    }
}

class DetailsScreen(private val releaseId: ReleaseId) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return DetailFragment.newInstance(releaseId)
    }
}

class DetailOtherGuidedScreen(private val releaseId: ReleaseId) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return DetailOtherGuidedFragment.newInstance(releaseId)
    }
}

class ScheduleScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ScheduleFragment()
    }
}

class UpdateScreen
    : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return UpdateFragment()
    }
}

class UpdateSourceScreen : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return UpdateSourceGuidedFragment()
    }
}

class SuggestionsScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return SuggestionsFragment()
    }
}

class SearchScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return SearchFragment()
    }
}

class SearchYearGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return SearchYearGuidedFragment().putValues(values)
    }
}

class SearchSeasonGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return SearchSeasonGuidedFragment().putValues(values)
    }
}

class SearchGenreGuidedScreen(private val values: List<String>) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return SearchGenreGuidedFragment().putValues(values)
    }
}

class SearchSortGuidedScreen(private val sort: SearchForm.Sort) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return SearchSortGuidedFragment.newInstance(sort)
    }
}

class SearchCompletedGuidedScreen(private val onlyCompleted: Boolean) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return SearchCompletedGuidedFragment.newInstance(onlyCompleted)
    }
}

class TestScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return TestFragment()
    }
}

class AuthGuidedScreen : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return AuthGuidedFragment()
    }
}

class AuthCredentialsGuidedScreen : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return AuthCredentialsGuidedFragment()
    }
}

class AuthOtpGuidedScreen : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return AuthOtpGuidedFragment()
    }
}

class PlayerScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return PlayerFragment.newInstance(releaseId, episodeId)
    }
}

class PlayerQualityGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerQualityGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerSpeedGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerSpeedGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEpisodesGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return PlayerEpisodesGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEndEpisodeGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return EndEpisodeGuidedFragment().putIds(releaseId, episodeId)
    }
}

class PlayerEndSeasonGuidedScreen(
    private val releaseId: ReleaseId,
    private val episodeId: EpisodeId?,
) : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return EndSeasonGuidedFragment().putIds(releaseId, episodeId)
    }
}

class TestGuidedStepScreen : GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): FakeGuidedStepFragment {
        return DialogExampleFragment()
    }
}

class MovieDetailsScreen(private val tmdbId: Int) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ru.radiationx.lexxie.screen.moviedetails.MovieDetailsFragment.newInstance(tmdbId)
    }
}

class TvSeriesDetailsScreen(private val tmdbId: Int) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ru.radiationx.lexxie.screen.tvseriesdetails.TvSeriesDetailsFragment.newInstance(tmdbId)
    }
}

class MovieSearchScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ContentSearchFragment.newInstance(ContentSearchType.MOVIE)
    }
}

class TvSeriesSearchScreen : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        return ContentSearchFragment.newInstance(ContentSearchType.TV_SERIES)
    }
}

class ContentPlayerScreen(
    private val tmdbId: Int,
    private val isMovie: Boolean,
    private val season: Int = 1,
    private val episode: Int = 1
) : FragmentScreen {
    override fun createFragment(factory: FragmentFactory): Fragment {
        val type = if (isMovie) {
            ru.radiationx.lexxie.screen.player.content.PlayerContentType.MOVIE
        } else {
            ru.radiationx.lexxie.screen.player.content.PlayerContentType.TV_SERIES
        }
        return ru.radiationx.lexxie.screen.player.content.ContentPlayerFragment.newInstance(
            tmdbId,
            type,
            season,
            episode
        )
    }
}

class ContentEpisodesScreen(
    private val tmdbId: Int,
    private val seasonNumber: Int = 1,
    private val episodeNumber: Int = 1
) : ru.radiationx.lexxie.common.fragment.GuidedAppScreen() {
    override fun createFragment(factory: FragmentFactory): ru.radiationx.lexxie.common.fragment.FakeGuidedStepFragment {
        return ContentEpisodesFragment.newInstance(tmdbId, seasonNumber, episodeNumber)
    }
}