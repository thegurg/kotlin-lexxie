package ru.radiationx.lexxie.common

import ru.radiationx.data.entity.domain.types.ReleaseId

data class LibriaCard(
    val title: String,
    val description: String,
    val image: String,
    val type: Type
) : CardItem {

    override fun getId(): Int {
        return type.hashCode()
    }

    sealed class Type {
        data class Release(val releaseId: ReleaseId) : Type()
        data class Youtube(val link: String) : Type()
        data class Movie(val tmdbId: Int) : Type()
        data class TvSeries(val tmdbId: Int) : Type()
    }
}