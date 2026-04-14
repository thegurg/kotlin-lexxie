package ru.radiationx.lexxie.screen.mainpages

import androidx.fragment.app.Fragment
import androidx.leanback.widget.Row
import ru.radiationx.lexxie.common.CachedRowsFragmentFactory
import ru.radiationx.lexxie.screen.main.MainFragment
import ru.radiationx.lexxie.screen.movies.MoviesFragment
import ru.radiationx.lexxie.screen.profile.ProfileFragment
import ru.radiationx.lexxie.screen.tvseries.TvSeriesFragment
import ru.radiationx.lexxie.screen.watching.WatchingFragment
import ru.radiationx.lexxie.screen.youtube.YoutubeFragment

class MainPagesFragmentFactory : CachedRowsFragmentFactory() {

    companion object {
        const val ID_MAIN = 1L
        const val ID_MY = 2L
        const val ID_MOVIES = 3L
        const val ID_SERIES = 4L
        const val ID_SEARCH = 5L
        const val ID_YOUTUBE = 6L
        const val ID_PROFILE = 7L

        val ids = listOf(
            ID_MAIN,
            ID_MY,
            ID_MOVIES,
            ID_SERIES,
            //ID_SEARCH,
            //ID_YOUTUBE,
            ID_PROFILE
        )

        val variant1 = mapOf(
            ID_MAIN to "Главная",
            ID_MY to "Я смотрю",
            ID_MOVIES to "Фильмы",
            ID_SERIES to "Сериалы",
            ID_SEARCH to "Поиск",
            ID_YOUTUBE to "YouTube",
            ID_PROFILE to "Профиль"
        )
    }

    override fun getFragmentByRow(row: Row): Fragment {
        val fragment = when (row.id) {
            ID_MAIN -> MainFragment()
            ID_MY -> WatchingFragment()
            ID_MOVIES -> MoviesFragment()
            ID_SERIES -> TvSeriesFragment()
            ID_YOUTUBE -> YoutubeFragment()
            ID_PROFILE -> ProfileFragment()
            else -> super.getFragmentByRow(row)
        }
        return fragment
    }
}