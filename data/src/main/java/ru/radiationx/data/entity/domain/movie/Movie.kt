package ru.radiationx.data.entity.domain.movie

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int,
    val tmdbId: Int,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val releaseDate: String?,
    val runtime: Int?,
    val genres: List<Genre>,
    val tagline: String?,
    val imdbId: String?
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

    val releaseYear: String?
        get() = releaseDate?.take(4)

    val formattedRuntime: String?
        get() = runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            when {
                hours > 0 && minutes > 0 -> "${hours}ч ${minutes}мин"
                hours > 0 -> "${hours}ч"
                else -> "${minutes}мин"
            }
        }

    val formattedVoteAverage: String
        get() = String.format("%.1f", voteAverage)

    @Parcelize
    data class Genre(
        val id: Int,
        val name: String
    ) : Parcelable
}
