package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.SimpleClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.response.video.VidlinkStreamResponse
import javax.inject.Inject

class VidlinkApi @Inject constructor(
    @SimpleClient private val client: IClient,
    private val moshi: Moshi
) {

    companion object {
        const val BASE_URL = "https://vidlink.pro"
        const val PROXY_URL = "https://vidlink.pro/proxy"
    }

    suspend fun getMovieStream(tmdbId: String): VidlinkStreamResponse {
        val url = "$PROXY_URL/movie/$tmdbId"
        return client.get(url, emptyMap())
            .fetchApiResponse(moshi)
    }

    suspend fun getTvStream(tmdbId: String, season: Int, episode: Int): VidlinkStreamResponse {
        val url = "$PROXY_URL/tv/$tmdbId/$season/$episode"
        return client.get(url, emptyMap())
            .fetchApiResponse(moshi)
    }
}
