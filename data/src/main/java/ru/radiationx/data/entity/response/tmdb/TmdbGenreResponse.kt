package ru.radiationx.data.entity.response.tmdb

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbGenreListResponse(
    @Json(name = "genres") val genres: List<TmdbGenreResponse>
)

@JsonClass(generateAdapter = true)
data class TmdbGenreResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?
)
