package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.TmdbClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchResponse
import ru.radiationx.data.entity.response.tmdb.TmdbGenreListResponse
import ru.radiationx.data.entity.response.tmdb.TmdbMovieDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbMovieListResponse
import ru.radiationx.data.entity.response.tmdb.TmdbSeasonDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbTvDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbTvListResponse
import javax.inject.Inject

class TmdbApi @Inject constructor(
    @TmdbClient private val client: IClient,
    private val moshi: Moshi
) {

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    }

    suspend fun getPopularMovies(page: Int): TmdbMovieListResponse {
        val args = mapOf(
            "page" to page.toString(),
            "sort_by" to "popularity.desc",
            "include_adult" to "false"
        )
        return client.get("$BASE_URL/discover/movie", args)
            .fetchResponse(moshi)
    }

    suspend fun getTopRatedMovies(page: Int): TmdbMovieListResponse {
        val args = mapOf(
            "page" to page.toString()
        )
        return client.get("$BASE_URL/movie/top_rated", args)
            .fetchResponse(moshi)
    }

    suspend fun getLatestMovies(page: Int): TmdbMovieListResponse {
        val args = mapOf(
            "page" to page.toString(),
            "sort_by" to "release_date.desc"
        )
        return client.get("$BASE_URL/discover/movie", args)
            .fetchResponse(moshi)
    }

    suspend fun getMovieDetails(id: Int): TmdbMovieDetailsResponse {
        val args = emptyMap<String, String>()
        return client.get("$BASE_URL/movie/$id", args)
            .fetchResponse(moshi)
    }

    suspend fun searchMovies(query: String, page: Int): TmdbMovieListResponse {
        val args = mapOf(
            "query" to query,
            "page" to page.toString(),
            "include_adult" to "false"
        )
        return client.get("$BASE_URL/search/movie", args)
            .fetchResponse(moshi)
    }

    suspend fun getPopularTv(page: Int): TmdbTvListResponse {
        val args = mapOf(
            "page" to page.toString()
        )
        return client.get("$BASE_URL/tv/popular", args)
            .fetchResponse(moshi)
    }

    suspend fun getTopRatedTv(page: Int): TmdbTvListResponse {
        val args = mapOf(
            "page" to page.toString()
        )
        return client.get("$BASE_URL/tv/top_rated", args)
            .fetchResponse(moshi)
    }

    suspend fun getLatestTv(page: Int): TmdbTvListResponse {
        val args = mapOf(
            "page" to page.toString(),
            "sort_by" to "first_air_date.desc"
        )
        return client.get("$BASE_URL/discover/tv", args)
            .fetchResponse(moshi)
    }

    suspend fun getTvDetails(id: Int): TmdbTvDetailsResponse {
        val args = emptyMap<String, String>()
        return client.get("$BASE_URL/tv/$id", args)
            .fetchResponse(moshi)
    }

    suspend fun searchTv(query: String, page: Int): TmdbTvListResponse {
        val args = mapOf(
            "query" to query,
            "page" to page.toString(),
            "include_adult" to "false"
        )
        return client.get("$BASE_URL/search/tv", args)
            .fetchResponse(moshi)
    }

    suspend fun getMovieGenres(): TmdbGenreListResponse {
        val args = emptyMap<String, String>()
        return client.get("$BASE_URL/genre/movie/list", args)
            .fetchResponse(moshi)
    }

    suspend fun getTvGenres(): TmdbGenreListResponse {
        val args = emptyMap<String, String>()
        return client.get("$BASE_URL/genre/tv/list", args)
            .fetchResponse(moshi)
    }

    suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): TmdbSeasonDetailsResponse {
        val args = emptyMap<String, String>()
        return client.get("$BASE_URL/tv/$tvId/season/$seasonNumber", args)
            .fetchResponse(moshi)
    }
}
