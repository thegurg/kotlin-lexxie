package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.tv.TvSeries
import ru.radiationx.shared.ktx.capitalizeDefault
import javax.inject.Inject

class TvSeriesDetailDataConverter @Inject constructor() {

    fun toDetail(tvSeries: TvSeries): LibriaDetails = tvSeries.run {
        LibriaDetails(
            id = null,
            titleRu = name,
            titleEn = originalName.orEmpty(),
            extra = listOfNotNull(
                firstAirYear,
                genres.firstOrNull()?.name?.capitalizeDefault(),
                seasonsInfo
            ).joinToString(" • ") + " ⭐ $formattedVoteAverage",
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
