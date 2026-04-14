package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.api.TmdbApi
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.movie.Movie
import ru.radiationx.data.entity.mapper.toDomainMovie
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val tmdbApi: TmdbApi
) {

    suspend fun getPopularMovies(page: Int): Paginated<Movie> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getPopularMovies(page)
        Paginated(
            data = response.results.map { it.toDomainMovie() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getTopRatedMovies(page: Int): Paginated<Movie> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getTopRatedMovies(page)
        Paginated(
            data = response.results.map { it.toDomainMovie() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getLatestMovies(page: Int): Paginated<Movie> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getLatestMovies(page)
        Paginated(
            data = response.results.map { it.toDomainMovie() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getMovieDetails(tmdbId: Int): Movie = withContext(Dispatchers.IO) {
        tmdbApi.getMovieDetails(tmdbId).toDomainMovie()
    }

    suspend fun searchMovies(query: String, page: Int): Paginated<Movie> = withContext(Dispatchers.IO) {
        val response = tmdbApi.searchMovies(query, page)
        Paginated(
            data = response.results.map { it.toDomainMovie() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }
}
