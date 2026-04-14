package ru.radiationx.data.repository

import ru.radiationx.data.datasource.remote.api.CustomStreamApi
import ru.radiationx.data.datasource.remote.api.VidSrcApi
import ru.radiationx.data.datasource.remote.api.VidlinkApi
import ru.radiationx.data.entity.domain.video.ContentType
import ru.radiationx.data.entity.domain.video.StreamInfo
import ru.radiationx.data.entity.response.video.VidsrcStreamResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoSourceRepository @Inject constructor(
    private val vidlinkApi: VidlinkApi,
    private val vidSrcApi: VidSrcApi,
    private val customStreamApi: CustomStreamApi
) {

    companion object {
        var customSourceUrl: String? = null
    }

    suspend fun getStreamInfo(
        tmdbId: String,
        type: ContentType,
        season: Int? = null,
        episode: Int? = null
    ): Result<StreamInfo> {
        if (!customSourceUrl.isNullOrEmpty()) {
            try {
                val customUrl = buildCustomUrl(customSourceUrl!!, tmdbId, type, season, episode)
                Timber.d("Trying custom source: $customUrl")
                val customResult = getCustomStream(customUrl)
                if (customResult.isSuccess) {
                    Timber.d("Custom source succeeded")
                    return customResult
                }
                Timber.d("Custom source failed: ${customResult.exceptionOrNull()?.message}")
            } catch (e: Exception) {
                Timber.d("Custom source exception: ${e.message}")
            }
        }

        try {
            val vidlinkResult = getVidlinkStream(tmdbId, type, season, episode)
            if (vidlinkResult.isSuccess) {
                val info = vidlinkResult.getOrNull()
                if (info?.m3u8Url != null) {
                    return vidlinkResult
                }
            }
            Timber.d("Vidlink failed or returned embed URL: ${vidlinkResult.exceptionOrNull()?.message}")
        } catch (e: Exception) {
            Timber.d("Vidlink exception: ${e.message}")
        }

        try {
            val vidsrcResult = getVidsrcStream(tmdbId, type, season, episode)
            if (vidsrcResult.isSuccess) {
                val info = vidsrcResult.getOrNull()
                if (info?.m3u8Url != null) {
                    return vidsrcResult
                }
            }
            Timber.d("VidSrc.to failed or returned embed URL: ${vidsrcResult.exceptionOrNull()?.message}")
        } catch (e: Exception) {
            Timber.d("VidSrc.to exception: ${e.message}")
        }

        try {
            val vidsrcMovResult = getVidsrcMovStream(tmdbId, type, season, episode)
            if (vidsrcMovResult.isSuccess) {
                val info = vidsrcMovResult.getOrNull()
                if (info?.m3u8Url != null) {
                    return vidsrcMovResult
                }
            }
            Timber.d("VidSrc.mov failed or returned embed URL: ${vidsrcMovResult.exceptionOrNull()?.message}")
        } catch (e: Exception) {
            Timber.d("VidSrc.mov exception: ${e.message}")
        }

        return Result.failure(NoStreamAvailableException())
    }

    private fun buildCustomUrl(baseUrl: String, tmdbId: String, type: ContentType, season: Int?, episode: Int?): String {
        return baseUrl
            .replace("{tmdb}", tmdbId)
            .replace("{type}", if (type == ContentType.MOVIE) "movie" else "tv")
            .replace("{season}", (season ?: 1).toString())
            .replace("{episode}", (episode ?: 1).toString())
    }

    private suspend fun getCustomStream(url: String): Result<StreamInfo> {
        return try {
            val response = customStreamApi.getStream(url)
            
            val m3u8Url = response.url ?: response.m3u8 ?: response.streamUrl
            
            if (m3u8Url.isNullOrEmpty()) {
                return Result.failure(NoStreamAvailableException("Custom source returned empty URL"))
            }

            Result.success(
                StreamInfo(
                    embedUrl = "",
                    m3u8Url = m3u8Url,
                    subtitles = emptyList()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getVidlinkStream(
        tmdbId: String,
        type: ContentType,
        season: Int?,
        episode: Int?
    ): Result<StreamInfo> {
        return try {
            val response = when (type) {
                ContentType.MOVIE -> vidlinkApi.getMovieStream(tmdbId)
                ContentType.TV -> {
                    if (season != null && episode != null) {
                        vidlinkApi.getTvStream(tmdbId, season, episode)
                    } else {
                        return Result.failure(IllegalArgumentException("Season and episode required for TV"))
                    }
                }
            }

            Timber.d("Vidlink response: success=${response.success}, sourcesSize=${response.sources?.size}")
            response.sources?.forEachIndexed { index, source ->
                Timber.d("  source[$index]: file=${source.file}, type=${source.type}, quality=${source.quality}")
            }

            if (response.success == false || response.sources.isNullOrEmpty()) {
                return Result.failure(NoStreamAvailableException("Vidlink returned empty sources"))
            }

            val source = response.sources?.firstOrNull()
            val m3u8Url = source?.file

            if (m3u8Url.isNullOrEmpty()) {
                return Result.failure(NoStreamAvailableException("Vidlink source has empty file URL"))
            }

            Result.success(
                StreamInfo(
                    embedUrl = "",
                    m3u8Url = m3u8Url,
                    subtitles = response.tracks?.mapNotNull { track ->
                        if (track.kind == "captions" && track.file != null) {
                            ru.radiationx.data.entity.domain.video.SubtitleInfo(
                                url = track.file,
                                label = track.label ?: "Unknown",
                                language = track.label ?: "unknown"
                            )
                        } else null
                    }.orEmpty()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getVidsrcStream(
        tmdbId: String,
        type: ContentType,
        season: Int?,
        episode: Int?
    ): Result<StreamInfo> {
        return try {
            val response: VidsrcStreamResponse = when (type) {
                ContentType.MOVIE -> vidSrcApi.getMovieStream(tmdbId)
                ContentType.TV -> {
                    if (season != null && episode != null) {
                        vidSrcApi.getTvStream(tmdbId, season, episode)
                    } else {
                        return Result.failure(IllegalArgumentException("Season and episode required for TV"))
                    }
                }
            }

            if (response.directUrl.isNullOrEmpty()) {
                return Result.failure(NoStreamAvailableException("VidSrc.to returned empty directUrl"))
            }

            Result.success(
                StreamInfo(
                    embedUrl = "",
                    m3u8Url = response.directUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getVidsrcMovStream(
        tmdbId: String,
        type: ContentType,
        season: Int?,
        episode: Int?
    ): Result<StreamInfo> {
        return try {
            val response: VidsrcStreamResponse = when (type) {
                ContentType.MOVIE -> vidSrcApi.getMovieStreamFromMov(tmdbId)
                ContentType.TV -> {
                    if (season != null && episode != null) {
                        vidSrcApi.getTvStreamFromMov(tmdbId, season, episode)
                    } else {
                        return Result.failure(IllegalArgumentException("Season and episode required for TV"))
                    }
                }
            }

            if (response.directUrl.isNullOrEmpty()) {
                return Result.failure(NoStreamAvailableException("VidSrc.mov returned empty directUrl"))
            }

            Result.success(
                StreamInfo(
                    embedUrl = "",
                    m3u8Url = response.directUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEmbedUrl(tmdbId: String, type: ContentType, season: Int? = null, episode: Int? = null): String {
        val base = when (type) {
            ContentType.MOVIE -> "https://vidlink.pro/movie/$tmdbId"
            ContentType.TV -> "https://vidlink.pro/tv/$tmdbId/${season ?: 1}/${episode ?: 1}"
        }
        return base
    }

    fun setCustomSource(url: String?) {
        customSourceUrl = url
    }
}

class NoStreamAvailableException(message: String = "No stream available") : Exception(message)
