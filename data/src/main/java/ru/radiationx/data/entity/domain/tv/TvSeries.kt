package ru.radiationx.data.entity.domain.tv

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TvSeries(
    val id: Int,
    val tmdbId: Int,
    val name: String,
    val originalName: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val episodeRunTime: List<Int>,
    val genres: List<Genre>,
    val status: String?,
    val tagline: String?,
    val type: String?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val inProduction: Boolean,
    val seasons: List<Season> = emptyList()
) : Parcelable {

    companion object {
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
        const val POSTER_SIZE = "w500"
        const val BACKDROP_SIZE = "w1280"
        const val ORIGINAL_SIZE = "original"
    }

    val posterUrl: String?
        get() = posterPath?.let { "$IMAGE_BASE_URL/$POSTER_SIZE$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "$IMAGE_BASE_URL/$BACKDROP_SIZE$it" }

    val originalPosterUrl: String?
        get() = posterPath?.let { "$IMAGE_BASE_URL/$ORIGINAL_SIZE$it" }

    val originalBackdropUrl: String?
        get() = backdropPath?.let { "$IMAGE_BASE_URL/$ORIGINAL_SIZE$it" }

    val firstAirYear: String?
        get() = firstAirDate?.take(4)

    val lastAirYear: String?
        get() = lastAirDate?.take(4)

    val formattedEpisodeRunTime: String?
        get() = episodeRunTime.firstOrNull()?.let { "${it}мин" }

    val formattedVoteAverage: String
        get() = String.format("%.1f", voteAverage)

    val seasonsInfo: String
        get() = "$numberOfSeasons ${declension(numberOfSeasons, "сезон", "сезона", "сезонов")}, " +
                "$numberOfEpisodes ${declension(numberOfEpisodes, "эпизод", "эпизода", "эпизодов")}"

    private fun declension(count: Int, one: String, few: String, many: String): String {
        val mod10 = count % 10
        val mod100 = count % 100
        return when {
            mod100 in 11..14 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }

    @Parcelize
    data class Genre(
        val id: Int,
        val name: String
    ) : Parcelable

    @Parcelize
    data class Season(
        val id: Int,
        val name: String,
        val overview: String?,
        val posterPath: String?,
        val seasonNumber: Int,
        val airDate: String?,
        val episodeCount: Int,
        val episodes: List<Episode> = emptyList()
    ) : Parcelable {

        val posterUrl: String?
            get() = posterPath?.let { "$IMAGE_BASE_URL/$POSTER_SIZE$it" }

        val airYear: String?
            get() = airDate?.take(4)
    }

    @Parcelize
    data class Episode(
        val id: Int,
        val name: String,
        val overview: String?,
        val stillPath: String?,
        val episodeNumber: Int,
        val seasonNumber: Int,
        val airDate: String?,
        val runtime: Int?
    ) : Parcelable {

        val stillUrl: String?
            get() = stillPath?.let { "$IMAGE_BASE_URL/w300$it" }
    }
}
