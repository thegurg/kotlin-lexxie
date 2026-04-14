package ru.radiationx.data.entity.domain.video

data class StreamInfo(
    val embedUrl: String,
    val m3u8Url: String?,
    val subtitles: List<SubtitleInfo> = emptyList()
)

data class SubtitleInfo(
    val url: String,
    val label: String,
    val language: String
)

enum class ContentType {
    MOVIE,
    TV
}
