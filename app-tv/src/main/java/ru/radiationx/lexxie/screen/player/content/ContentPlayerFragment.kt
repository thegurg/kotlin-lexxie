package ru.radiationx.lexxie.screen.player.content

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.ListRow
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import ru.radiationx.lexxie.screen.player.VideoPlayerGlue
import ru.radiationx.lexxie.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.quill.viewModel
import timber.log.Timber

@UnstableApi
class ContentPlayerFragment : VideoSupportFragment() {

    companion object {
        private const val ARG_CONTENT_ID = "content_id"
        private const val ARG_CONTENT_TYPE = "content_type"
        private const val ARG_SEASON = "season"
        private const val ARG_EPISODE = "episode"

        private const val SOURCE_TIMEOUT_MS = 30000L

        fun newInstance(
            tmdbId: Int,
            type: PlayerContentType,
            season: Int = 1,
            episode: Int = 1
        ): ContentPlayerFragment = ContentPlayerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CONTENT_ID, tmdbId)
                putString(ARG_CONTENT_TYPE, type.name)
                putInt(ARG_SEASON, season)
                putInt(ARG_EPISODE, episode)
            }
        }
    }

/* ==============================================
 * COMMENTED OUT VIDEO SOURCES
 * Will be uncommented later when needed
 * ==============================================

    data class VideoSource(
        val name: String,
        val movieUrl: String,
        val tvUrl: String
    )

    private val videoSources = listOf(
        VideoSource(
            name = "flixer.su",
            movieUrl = "https://flixer.su/watch/movie/%d",
            tvUrl = "https://flixer.su/watch/tv/%d/%d/%d"
        ),
        VideoSource(
            name = "vidsrc.to",
            movieUrl = "https://vidsrc.to/embed/movie/%d",
            tvUrl = "https://vidsrc.to/embed/tv/%d/%d/%d"
        ),
        VideoSource(
            name = "vidsrc.icu",
            movieUrl = "https://vidsrc.icu/embed/movie/%d",
            tvUrl = "https://vidsrc.icu/embed/tv/%d/%d/%d"
        ),
        VideoSource(
            name = "vidsrc-embed.ru",
            movieUrl = "https://vidsrc-embed.ru/embed/movie?tmdb=%d",
            tvUrl = "https://vidsrc-embed.ru/embed/tv?tmdb=%d"
        ),
        VideoSource(
            name = "vidking.net",
            movieUrl = "https://www.vidking.net/embed/movie/%d",
            tvUrl = "https://www.vidking.net/embed/tv/%d/%d/%d"
        )
    )

 * END OF COMMENTED OUT SOURCES
 * ============================================== */

    private val argExtra by lazy {
        ContentPlayerExtra(
            contentId = arguments?.getInt(ARG_CONTENT_ID) ?: 0,
            contentType = PlayerContentType.valueOf(arguments?.getString(ARG_CONTENT_TYPE) ?: PlayerContentType.MOVIE.name),
            season = arguments?.getInt(ARG_SEASON) ?: 1,
            episode = arguments?.getInt(ARG_EPISODE) ?: 1
        )
    }

    private val viewModel by viewModel<ContentPlayerViewModel> { argExtra }

    @Suppress("DEPRECATION")
    private var playerGlue: VideoPlayerGlue? = null
        private set

    @Suppress("DEPRECATION")
    private var player: ExoPlayer? = null
        private set

    private var extractorWebView: WebView? = null
    private var isExtracting = false
    private var timeoutHandler: Handler? = null
    private var foundM3u8Url: String? = null

    private fun getFlixerUrl(): String {
        return when (argExtra.contentType) {
            PlayerContentType.MOVIE -> "https://flixer.su/watch/movie/${argExtra.contentId}"
            PlayerContentType.TV_SERIES -> "https://flixer.su/watch/tv/${argExtra.contentId}/${argExtra.season}/${argExtra.episode}"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Timber.d("ContentPlayerFragment onViewCreated - flixer.su with improved sniffing")

        initializePlayer()
        initializeRows()

        timeoutHandler = Handler(Looper.getMainLooper())

        startExtraction()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startExtraction() {
        if (isExtracting) return
        isExtracting = true
        foundM3u8Url = null

        val embedUrl = getFlixerUrl()
        Timber.d("Starting extraction with flixer.su: $embedUrl")

        destroyExtractor()

        extractorWebView = WebView(requireContext()).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
                mediaPlaybackRequiresUserGesture = false
                userAgentString = "Mozilla/5.0 (Linux; Android 11; Build/RKQ1.200826.002) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36"
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    Timber.d("flixer.su WebView progress: $newProgress%")
                    if (newProgress >= 80) {
                        view?.postDelayed({
                            Timber.d("flixer.su: Injecting improved extraction script")
                            injectExtractionScript()
                        }, 3000)
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): android.webkit.WebResourceResponse? {
                    val url = request?.url?.toString()
                    if (url != null && url.contains(".m3u8")) {
                        Timber.d("flixer.su: [shouldIntercept] Found m3u8: ${url.take(100)}")
                        foundM3u8Url = url
                        requireActivity().runOnUiThread {
                            isExtracting = false
                            destroyExtractor()
                            playVideo(url)
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Timber.d("flixer.su: Page finished: $url")
                }
            }

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onM3u8Found(url: String) {
                    Timber.d("flixer.su: [JS] Found m3u8: $url")
                    foundM3u8Url = url
                    requireActivity().runOnUiThread {
                        isExtracting = false
                        destroyExtractor()
                        playVideo(url)
                    }
                }
            }, "AndroidBridge")

            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(1, 1)

            loadUrl(embedUrl)
        }

        view?.let { (it as? ViewGroup)?.addView(extractorWebView) }

        timeoutHandler?.postDelayed({
            if (isExtracting) {
                Timber.e("flixer.su: Extraction timeout, foundM3u8: ${foundM3u8Url != null}")
                isExtracting = false
                destroyExtractor()
                viewModel.onExtractionFailed("Не удалось найти видео")
            }
        }, SOURCE_TIMEOUT_MS)
    }

    private fun injectExtractionScript() {
        val script = """
            (function() {
                console.log('flixer.su: Extraction script starting');
                
                function findM3U8() {
                    var result = null;
                    
                    // Method 1: performance.getEntries() - NEW!
                    console.log('flixer.su: Trying Method 1 - performance.getEntries()');
                    try {
                        var entries = performance.getEntriesByType('resource');
                        console.log('flixer.su: Found ' + entries.length + ' resource entries');
                        for (var i = 0; i < entries.length; i++) {
                            var name = entries[i].name || entries[i].url || '';
                            if (name.includes('.m3u8')) {
                                console.log('flixer.su: Found m3u8 in performance: ' + name);
                                result = name;
                                break;
                            }
                        }
                    } catch(e) {
                        console.log('flixer.su: performance.getEntries error: ' + e.message);
                    }
                    
                    // Method 2: Direct video element
                    if (!result) {
                        console.log('flixer.su: Trying Method 2 - video elements');
                        var videos = document.querySelectorAll('video');
                        for (var v = 0; v < videos.length; v++) {
                            var src = videos[v].src;
                            if (src && src.includes('.m3u8')) {
                                result = src;
                                break;
                            }
                            var sources = videos[v].querySelectorAll('source');
                            for (var s = 0; s < sources.length; s++) {
                                if (sources[s].src && sources[s].src.includes('.m3u8')) {
                                    result = sources[s].src;
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Method 3: window.player object
                    if (!result) {
                        console.log('flixer.su: Trying Method 3 - window.player');
                        if (window.player) {
                            console.log('flixer.su: Found window.player');
                            if (window.player.src && window.player.src.includes('.m3u8')) {
                                result = window.player.src;
                            } else if (window.player.sources && window.player.sources[0]) {
                                result = window.player.sources[0].src || window.player.sources[0].file;
                            } else if (window.player.currentSrc && window.player.currentSrc.includes('.m3u8')) {
                                result = window.player.currentSrc;
                            }
                        }
                    }
                    
                    // Method 4: HLS.js instances
                    if (!result) {
                        console.log('flixer.su: Trying Method 4 - HLS.js instances');
                        for (var key in window) {
                            try {
                                if (window[key] && typeof window[key].loadSource === 'function') {
                                    var url = window[key].url || window[key]._url;
                                    if (url && url.includes('.m3u8')) {
                                        result = url;
                                        break;
                                    }
                                }
                            } catch(e) {}
                        }
                    }
                    
                    // Method 5: data-src attributes
                    if (!result) {
                        console.log('flixer.su: Trying Method 5 - data-src attributes');
                        var elements = document.querySelectorAll('[data-src]');
                        for (var i = 0; i < elements.length; i++) {
                            var ds = elements[i].getAttribute('data-src');
                            if (ds && ds.includes('.m3u8')) {
                                result = ds;
                                break;
                            }
                        }
                    }
                    
                    // Method 6: iframe src
                    if (!result) {
                        console.log('flixer.su: Trying Method 6 - iframes');
                        var iframes = document.querySelectorAll('iframe');
                        for (var i = 0; i < iframes.length; i++) {
                            var src = iframes[i].src;
                            if (src && (src.includes('workers.dev') || src.includes('.m3u8'))) {
                                result = src;
                                break;
                            }
                        }
                    }
                    
                    // Method 7: script tags regex
                    if (!result) {
                        console.log('flixer.su: Trying Method 7 - script tags');
                        var scripts = document.querySelectorAll('script');
                        for (var i = 0; i < scripts.length; i++) {
                            var content = scripts[i].textContent;
                            if (content) {
                                var match = content.match(/https?:\/\/[^\s"'<>]+\.m3u8[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                                match = content.match(/https?:\/\/[^\s"'<>]*workers\.dev[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                                match = content.match(/https?:\/\/[^\s"'<>]*(?:rainorbit|thunderleaf)[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                            }
                        }
                    }
                    
                    console.log('flixer.su: Final result: ' + (result || 'null'));
                    return result;
                }
                
                var found = false;
                function tryFind() {
                    if (found) return;
                    var url = findM3U8();
                    if (url) {
                        found = true;
                        console.log('flixer.su: Sending to Android: ' + url);
                        window.AndroidBridge.onM3u8Found(url);
                    } else {
                        console.log('flixer.su: Not found, retrying in 1 sec...');
                        setTimeout(tryFind, 1000);
                    }
                }
                tryFind();
            })();
        """.trimIndent()

        try {
            extractorWebView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to inject script")
        }
    }

    private fun playVideo(url: String) {
        Timber.d("Playing video: $url")

        val currentPlayer = player ?: return

        currentPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        Timber.d("Player ready")
                        logTrackInfo()
                    }
                    Player.STATE_BUFFERING -> Timber.d("Player buffering")
                    Player.STATE_ENDED -> Timber.d("Playback ended")
                    Player.STATE_IDLE -> Timber.d("Player idle")
                }
            }

            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                logTrackInfo()
            }
        })

        currentPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        currentPlayer.prepare()
        currentPlayer.play()
    }

    private fun logTrackInfo() {
        val videoPlayer = player ?: return
        val trackSelector = videoPlayer.currentTracks
        Timber.d("=== VIDEO TRACK INFO ===")
        trackSelector.groups.forEachIndexed { groupIndex, group ->
            for (trackIndex in 0 until group.length) {
                if (group.isTrackSupported(trackIndex)) {
                    val format = group.getTrackFormat(trackIndex)
                    val trackType = when {
                        format.sampleMimeType?.startsWith("video/") == true -> "VIDEO"
                        format.sampleMimeType?.startsWith("audio/") == true -> "AUDIO"
                        else -> "OTHER"
                    }
                    Timber.d("$trackType track [$groupIndex:$trackIndex]: codec=${format.sampleMimeType}, " +
                            "res=${format.width}x${format.height}, bitrate=${format.bitrate}, " +
                            "language=${format.language}")
                }
            }
        }
        Timber.d("========================")
    }

    private fun destroyExtractor() {
        extractorWebView?.apply {
            stopLoading()
            clearCache(true)
            clearHistory()
            removeJavascriptInterface("AndroidBridge")
            val parent = parent as? ViewGroup
            parent?.removeView(this)
            destroy()
        }
        extractorWebView = null
    }

    @Suppress("DEPRECATION")
    @UnstableApi
    private fun initializePlayer() {
        if (player != null) {
            return
        }

        val context = requireContext()

        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(context).apply {
            setDataSourceFactory(dataSourceFactory)
        }

        val exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val playerAdapter = LeanbackPlayerAdapter(context, exoPlayer, 500)

        val glue = VideoPlayerGlue(context, playerAdapter).apply {
            host = VideoSupportFragmentGlueHost(this@ContentPlayerFragment)
        }

        this.player = exoPlayer
        this.playerGlue = glue
    }

    @UnstableApi
    private fun initializeRows() {
        val glue = playerGlue ?: return
        val controlsRow = glue.controlsRow ?: return

        val rowsPresenter = ClassPresenterSelector().apply {
            addClassPresenter(ListRow::class.java, CustomListRowPresenter())
            addClassPresenter(controlsRow.javaClass, glue.playbackRowPresenter)
        }
        val rowsAdapter = ArrayObjectAdapter(rowsPresenter).apply {
            add(controlsRow)
        }

        adapter = rowsAdapter
    }

    override fun onPause() {
        super.onPause()
        playerGlue?.pause()
    }

    @UnstableApi
    override fun onDestroyView() {
        timeoutHandler?.removeCallbacksAndMessages(null)
        destroyExtractor()
        player?.release()
        player = null
        playerGlue = null
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroyView()
    }
}
