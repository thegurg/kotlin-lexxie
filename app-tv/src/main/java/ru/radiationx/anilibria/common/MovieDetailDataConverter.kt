package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.movie.Movie
import ru.radiationx.shared.ktx.capitalizeDefault
import javax.inject.Inject

class MovieDetailDataConverter @Inject constructor() {

    fun toDetail(movie: Movie): LibriaDetails = movie.run {
        LibriaDetails(
            id = null,
            titleRu = title,
            titleEn = originalTitle.orEmpty(),
            extra = listOfNotNull(
                releaseYear,
                genres.firstOrNull()?.name?.capitalizeDefault(),
                formattedRuntime,
                "⭐ $formattedVoteAverage"
            ).joinToString(" • "),
            description = overview.orEmpty(),
            announce = "",
            image = posterUrl.orEmpty(),
            favoriteCount = "",
            hasFullHd = true,
            isFavorite = false,
            hasEpisodes = true,
            hasViewed = false,
            hasWebPlayer = true
        )
    }
}
