package ru.radiationx.data.entity.response.tmdb

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbTvListResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<TmdbTvResponse>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

@JsonClass(generateAdapter = true)
data class TmdbTvResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "popularity") val popularity: Double?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "genre_ids") val genreIds: List<Int>?,
    @Json(name = "origin_country") val originCountry: List<String>?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name = "adult") val adult: Boolean?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetailsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "popularity") val popularity: Double?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "last_air_date") val lastAirDate: String?,
    @Json(name = "episode_run_time") val episodeRunTime: List<Int>?,
    @Json(name = "genres") val genres: List<TmdbGenreResponse>?,
    @Json(name = "status") val status: String?,
    @Json(name = "tagline") val tagline: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?,
    @Json(name = "origin_country") val originCountry: List<String>?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name = "adult") val adult: Boolean?,
    @Json(name = "in_production") val inProduction: Boolean?,
    @Json(name = "languages") val languages: List<String>?,
    @Json(name = "created_by") val createdBy: List<TmdbCreatedByResponse>?,
    @Json(name = "seasons") val seasons: List<TmdbSeasonResponse>?
)

@JsonClass(generateAdapter = true)
data class TmdbCreatedByResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "profile_path") val profilePath: String?
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "air_date") val airDate: String?,
    @Json(name = "episode_count") val episodeCount: Int?
)
