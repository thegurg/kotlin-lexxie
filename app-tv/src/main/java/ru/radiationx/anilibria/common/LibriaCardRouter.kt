package ru.radiationx.anilibria.common

import com.github.terrakok.cicerone.Router
import ru.radiationx.anilibria.screen.DetailsScreen
import ru.radiationx.anilibria.screen.MovieDetailsScreen
import ru.radiationx.anilibria.screen.TvSeriesDetailsScreen
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

class LibriaCardRouter @Inject constructor(
    private val router: Router,
    private val systemUtils: SystemUtils
) {

    fun navigate(libriaCard: LibriaCard) {
        when (val type = libriaCard.type) {
            is LibriaCard.Type.Release -> {
                router.navigateTo(DetailsScreen(type.releaseId))
            }

            is LibriaCard.Type.Youtube -> {
                systemUtils.externalLink(type.link)
            }

            is LibriaCard.Type.Movie -> {
                router.navigateTo(MovieDetailsScreen(type.tmdbId))
            }

            is LibriaCard.Type.TvSeries -> {
                router.navigateTo(TvSeriesDetailsScreen(type.tmdbId))
            }
        }
    }
}