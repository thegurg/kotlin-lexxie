package ru.radiationx.data.entity.response.video

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomStreamResponse(
    @Json(name = "url") val url: String?,
    @Json(name = "m3u8") val m3u8: String?,
    @Json(name = "stream_url") val streamUrl: String?,
    @Json(name = "quality") val quality: String?,
    @Json(name = "success") val success: Boolean?
)

data class CustomStreamSourcesResponse(
    @Json(name = "sources") val sources: List<CustomSource>?,
    @Json(name = "success") val success: Boolean?
)

@JsonClass(generateAdapter = true)
data class CustomSource(
    @Json(name = "file") val file: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "quality") val quality: String?
)
