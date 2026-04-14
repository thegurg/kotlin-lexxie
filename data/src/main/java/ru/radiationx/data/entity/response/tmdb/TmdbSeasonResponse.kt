package ru.radiationx.data.entity.response.tmdb

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbSeasonDetailsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "air_date") val airDate: String?,
    @Json(name = "episodes") val episodes: List<TmdbEpisodeResponse>?
)

@JsonClass(generateAdapter = true)
data class TmdbEpisodeResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "still_path") val stillPath: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "episode_number") val episodeNumber: Int?,
    @Json(name = "air_date") val airDate: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "runtime") val runtime: Int?
)
