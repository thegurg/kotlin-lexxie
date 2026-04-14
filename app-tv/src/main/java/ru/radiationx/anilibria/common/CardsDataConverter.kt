package ru.radiationx.anilibria.common

import android.content.Context
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.movie.Movie
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared.ktx.capitalizeDefault
import ru.radiationx.shared.ktx.decapitalizeDefault
import java.util.Date

class CardsDataConverter(
    private val context: Context,
) {

    fun toCard(releaseItem: Release) = releaseItem.run {
        val torrentDate = torrentUpdate.takeIf { it != 0 }?.let { Date(it * 1000L) }
        val seasonText = "${year.orEmpty()} ${season.orEmpty()}"
        val genreText = genres.firstOrNull()?.capitalizeDefault()
        val seriesText = "Серии: ${series?.trim() ?: "Не доступно"}"
        val updateText = torrentDate?.let {
            "Обновлен ${it.relativeDate(context).decapitalizeDefault()}"
        }
        val descItems = listOfNotNull(seasonText, genreText, seriesText, updateText)
        LibriaCard(
            title.orEmpty(),
            descItems.joinToString(" • "),
            poster.orEmpty(),
            LibriaCard.Type.Release(releaseItem.id)
        )
    }

    fun toCard(youtubeItem: YoutubeItem) = youtubeItem.run {
        LibriaCard(
            title.orEmpty(),
            "Вышел ${Date(timestamp * 1000L).relativeDate(context).decapitalizeDefault()}",
            image.orEmpty(),
            LibriaCard.Type.Youtube(youtubeItem.link)
        )
    }

    fun toCard(movie: Movie) = movie.run {
        val yearText = releaseYear ?: ""
        val genreText = genres.firstOrNull()?.name?.capitalizeDefault() ?: ""
        val ratingText = "⭐ $formattedVoteAverage"
        val runtimeText = formattedRuntime
        val descItems = listOfNotNull(yearText, genreText, runtimeText, ratingText)
        LibriaCard(
            title,
            descItems.joinToString(" • "),
            posterUrl ?: "",
            LibriaCard.Type.Movie(tmdbId)
        )
    }

    fun toCard(tvSeries: TvSeries) = tvSeries.run {
        val yearText = firstAirYear ?: ""
        val genreText = genres.firstOrNull()?.name?.capitalizeDefault() ?: ""
        val ratingText = "⭐ $formattedVoteAverage"
        val descItems = listOfNotNull(yearText, genreText, ratingText)
        LibriaCard(
            name,
            descItems.joinToString(" • "),
            posterUrl ?: "",
            LibriaCard.Type.TvSeries(tmdbId)
        )
    }

    fun toCard(feedItem: FeedItem): LibriaCard = feedItem.run {
        when {
            release != null -> toCard(release!!)
            youtube != null -> toCard(youtube!!)
            else -> throw RuntimeException("WataFuq")
        }
    }
}