package ru.radiationx.data.di.providers

import okhttp3.OkHttpClient
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.appendTimeouts
import javax.inject.Inject
import javax.inject.Provider

class TmdbOkHttpProvider @Inject constructor(
    private val sharedBuildConfig: SharedBuildConfig,
) : Provider<OkHttpClient> {

    companion object {
        const val API_KEY = "fc53bf8eb4f0ed122e92287fbe0017fa"
    }

    override fun get(): OkHttpClient = OkHttpClient.Builder()
        .appendTimeouts()
        .addInterceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url

            val url = originalUrl.newBuilder()
                .addQueryParameter("api_key", API_KEY)
                .build()

            val request = original.newBuilder()
                .url(url)
                .header("User-Agent", "AniLibria/1.0")
                .build()

            chain.proceed(request)
        }
        .build()
}
