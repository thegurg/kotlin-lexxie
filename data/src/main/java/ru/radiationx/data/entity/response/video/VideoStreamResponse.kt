package ru.radiationx.data.entity.response.video

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VidlinkStreamResponse(
    @Json(name = "sources") val sources: List<VidlinkSource>?,
    @Json(name = "tracks") val tracks: List<VidlinkTrack>?,
    @Json(name = "success") val success: Boolean?,
    @Json(name = "error") val error: String?
)

@JsonClass(generateAdapter = true)
data class VidlinkSource(
    @Json(name = "file") val file: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "label") val label: String?,
    @Json(name = "quality") val quality: String?
)

@JsonClass(generateAdapter = true)
data class VidlinkTrack(
    @Json(name = "file") val file: String?,
    @Json(name = "label") val label: String?,
    @Json(name = "kind") val kind: String?,
    @Json(name = "default") val default: Boolean?
)

data class VidsrcStreamResponse(
    val embedUrl: String,
    val directUrl: String?,
    val success: Boolean
)
