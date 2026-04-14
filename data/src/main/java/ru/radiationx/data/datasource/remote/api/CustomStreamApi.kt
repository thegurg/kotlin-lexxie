package ru.radiationx.data.datasource.remote.api

import com.squareup.moshi.Moshi
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.datasource.remote.fetchApiResponse
import ru.radiationx.data.entity.response.video.CustomStreamResponse
import javax.inject.Inject

class CustomStreamApi @Inject constructor(
    private val client: IClient,
    private val moshi: Moshi
) {

    suspend fun getStream(url: String): CustomStreamResponse {
        return client.get(url, emptyMap())
            .fetchApiResponse(moshi)
    }
}
