package ru.radiationx.data.entity.response.tmdb

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbMovieListResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<TmdbMovieResponse>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

@JsonClass(generateAdapter = true)
data class TmdbMovieResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "popularity") val popularity: Double?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "genre_ids") val genreIds: List<Int>?,
    @Json(name = "adult") val adult: Boolean?,
    @Json(name = "video") val video: Boolean?,
    @Json(name = "original_language") val originalLanguage: String?
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetailsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "popularity") val popularity: Double?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "runtime") val runtime: Int?,
    @Json(name = "budget") val budget: Long?,
    @Json(name = "revenue") val revenue: Long?,
    @Json(name = "status") val status: String?,
    @Json(name = "tagline") val tagline: String?,
    @Json(name = "genres") val genres: List<TmdbGenreResponse>?,
    @Json(name = "production_companies") val productionCompanies: List<TmdbProductionCompanyResponse>?,
    @Json(name = "spoken_languages") val spokenLanguages: List<TmdbSpokenLanguageResponse>?,
    @Json(name = "adult") val adult: Boolean?,
    @Json(name = "imdb_id") val imdbId: String?,
    @Json(name = "original_language") val originalLanguage: String?
)

@JsonClass(generateAdapter = true)
data class TmdbProductionCompanyResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "logo_path") val logoPath: String?,
    @Json(name = "origin_country") val originCountry: String?
)

@JsonClass(generateAdapter = true)
data class TmdbSpokenLanguageResponse(
    @Json(name = "iso_639_1") val iso6391: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "english_name") val englishName: String?
)
