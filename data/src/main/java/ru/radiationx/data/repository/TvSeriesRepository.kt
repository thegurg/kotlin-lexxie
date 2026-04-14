package ru.radiationx.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.radiationx.data.datasource.remote.api.TmdbApi
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.entity.mapper.toDomainTv
import ru.radiationx.data.entity.mapper.toDomainSeason
import javax.inject.Inject

class TvSeriesRepository @Inject constructor(
    private val tmdbApi: TmdbApi
) {

    suspend fun getPopularTv(page: Int): Paginated<TvSeries> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getPopularTv(page)
        Paginated(
            data = response.results.map { it.toDomainTv() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getTopRatedTv(page: Int): Paginated<TvSeries> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getTopRatedTv(page)
        Paginated(
            data = response.results.map { it.toDomainTv() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getLatestTv(page: Int): Paginated<TvSeries> = withContext(Dispatchers.IO) {
        val response = tmdbApi.getLatestTv(page)
        Paginated(
            data = response.results.map { it.toDomainTv() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getTvDetails(tmdbId: Int): TvSeries = withContext(Dispatchers.IO) {
        tmdbApi.getTvDetails(tmdbId).toDomainTv()
    }

    suspend fun searchTv(query: String, page: Int): Paginated<TvSeries> = withContext(Dispatchers.IO) {
        val response = tmdbApi.searchTv(query, page)
        Paginated(
            data = response.results.map { it.toDomainTv() },
            page = response.page,
            allPages = response.totalPages,
            perPage = null,
            allItems = response.totalResults
        )
    }

    suspend fun getSeasonDetails(tmdbId: Int, seasonNumber: Int): TvSeries.Season = withContext(Dispatchers.IO) {
        tmdbApi.getSeasonDetails(tmdbId, seasonNumber).toDomainSeason()
    }
}
