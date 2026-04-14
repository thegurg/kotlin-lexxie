package ru.radiationx.data.entity.mapper

import ru.radiationx.data.entity.domain.movie.Movie
import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.data.entity.response.tmdb.TmdbMovieDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbMovieResponse
import ru.radiationx.data.entity.response.tmdb.TmdbSeasonDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbSeasonResponse
import ru.radiationx.data.entity.response.tmdb.TmdbTvDetailsResponse
import ru.radiationx.data.entity.response.tmdb.TmdbTvResponse
import ru.radiationx.data.entity.response.tmdb.TmdbGenreResponse

fun TmdbMovieResponse.toDomainMovie(): Movie {
    return Movie(
        id = 0,
        tmdbId = id,
        title = title.orEmpty(),
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        releaseDate = releaseDate,
        runtime = null,
        genres = emptyList(),
        tagline = null,
        imdbId = null
    )
}

fun TmdbMovieDetailsResponse.toDomainMovie(): Movie {
    return Movie(
        id = 0,
        tmdbId = id,
        title = title.orEmpty(),
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres?.map { it.toDomainMovieGenre() }.orEmpty(),
        tagline = tagline,
        imdbId = imdbId
    )
}

fun TmdbTvResponse.toDomainTv(): TvSeries {
    return TvSeries(
        id = 0,
        tmdbId = id,
        name = name.orEmpty(),
        originalName = originalName,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        firstAirDate = firstAirDate,
        lastAirDate = null,
        episodeRunTime = emptyList(),
        genres = emptyList(),
        status = null,
        tagline = null,
        type = null,
        numberOfSeasons = numberOfSeasons ?: 0,
        numberOfEpisodes = numberOfEpisodes ?: 0,
        inProduction = false
    )
}

fun TmdbTvDetailsResponse.toDomainTv(): TvSeries {
    return TvSeries(
        id = 0,
        tmdbId = id,
        name = name.orEmpty(),
        originalName = originalName,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        popularity = popularity ?: 0.0,
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        episodeRunTime = episodeRunTime.orEmpty(),
        genres = genres?.map { it.toDomainTvGenre() }.orEmpty(),
        status = status,
        tagline = tagline,
        type = type,
        numberOfSeasons = numberOfSeasons ?: 0,
        numberOfEpisodes = numberOfEpisodes ?: 0,
        inProduction = inProduction ?: false,
        seasons = seasons?.map { it.toDomainSeason() }.orEmpty()
    )
}

fun TmdbGenreResponse.toDomainMovieGenre(): Movie.Genre {
    return Movie.Genre(
        id = id,
        name = name.orEmpty()
    )
}

fun TmdbGenreResponse.toDomainTvGenre(): TvSeries.Genre {
    return TvSeries.Genre(
        id = id,
        name = name.orEmpty()
    )
}

fun TmdbSeasonDetailsResponse.toDomainSeason(): TvSeries.Season {
    return TvSeries.Season(
        id = id,
        name = name.orEmpty(),
        overview = overview,
        posterPath = posterPath,
        seasonNumber = seasonNumber ?: 0,
        airDate = airDate,
        episodeCount = episodes?.size ?: 0,
        episodes = episodes?.map { it.toDomainEpisode() }.orEmpty()
    )
}

fun TmdbSeasonResponse.toDomainSeason(): TvSeries.Season {
    return TvSeries.Season(
        id = id,
        name = name.orEmpty(),
        overview = overview,
        posterPath = posterPath,
        seasonNumber = seasonNumber ?: 0,
        airDate = airDate,
        episodeCount = episodeCount ?: 0
    )
}

fun ru.radiationx.data.entity.response.tmdb.TmdbEpisodeResponse.toDomainEpisode(): TvSeries.Episode {
    return TvSeries.Episode(
        id = id,
        name = name.orEmpty(),
        overview = overview,
        stillPath = stillPath,
        episodeNumber = episodeNumber ?: 0,
        seasonNumber = seasonNumber ?: 0,
        airDate = airDate,
        runtime = runtime
    )
}
