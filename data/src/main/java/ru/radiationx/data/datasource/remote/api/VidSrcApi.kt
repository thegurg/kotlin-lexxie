package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.SimpleClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.response.video.VidsrcStreamResponse
import javax.inject.Inject

class VidSrcApi @Inject constructor(
    @SimpleClient private val client: IClient,
    private val moshi: Moshi
) {

    companion object {
        const val BASE_URL = "https://vidsrc.to"
    }

    suspend fun getMovieStream(tmdbId: String): VidsrcStreamResponse {
        val url = "$BASE_URL/embed/movie/$tmdbId"
        return VidsrcStreamResponse(
            embedUrl = url,
            directUrl = null,
            success = true
        )
    }

    suspend fun getTvStream(tmdbId: String, season: Int, episode: Int): VidsrcStreamResponse {
        val url = "$BASE_URL/embed/tv/$tmdbId/$season/$episode"
        return VidsrcStreamResponse(
            embedUrl = url,
            directUrl = null,
            success = true
        )
    }

    suspend fun getMovieStreamFromMov(tmdbId: String): VidsrcStreamResponse {
        val url = "https://vidsrc.mov/embed/movie/$tmdbId"
        return VidsrcStreamResponse(
            embedUrl = url,
            directUrl = null,
            success = true
        )
    }

    suspend fun getTvStreamFromMov(tmdbId: String, season: Int, episode: Int): VidsrcStreamResponse {
        val url = "https://vidsrc.mov/embed/tv/$tmdbId/$season/$episode"
        return VidsrcStreamResponse(
            embedUrl = url,
            directUrl = null,
            success = true
        )
    }
}
