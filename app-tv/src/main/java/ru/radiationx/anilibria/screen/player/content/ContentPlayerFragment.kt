package ru.radiationx.anilibria.screen.player.content

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
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
import ru.radiationx.anilibria.screen.player.VideoPlayerGlue
import ru.radiationx.anilibria.ui.presenter.cust.CustomListRowPresenter
import ru.radiationx.quill.viewModel
import timber.log.Timber

@UnstableApi
class ContentPlayerFragment : VideoSupportFragment() {

    companion object {
        private const val ARG_CONTENT_ID = "content_id"
        private const val ARG_CONTENT_TYPE = "content_type"
        private const val ARG_SEASON = "season"
        private const val ARG_EPISODE = "episode"
        
        private const val FALLBACK_M3U8_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

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

    @SuppressLint("SetJavaScriptEnabled")
    @Suppress("DEPRECATION")
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        Timber.d("ContentPlayerFragment onViewCreated - WebView extraction")
        
        initializePlayer()
        initializeRows()
        
        startExtraction()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startExtraction() {
        if (isExtracting) return
        isExtracting = true

        val tmdbId = argExtra.contentId
        val embedUrl = when (argExtra.contentType) {
            PlayerContentType.MOVIE -> "https://flixer.su/watch/movie/$tmdbId"
            PlayerContentType.TV_SERIES -> "https://flixer.su/watch/tv/$tmdbId/${argExtra.season}/${argExtra.episode}"
        }
        Timber.d("Starting WebView extraction: $embedUrl")

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
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress >= 80) {
                        view?.postDelayed({
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
                        Timber.d("Intercepted m3u8 request: ${url.take(100)}")
                        requireActivity().runOnUiThread {
                            isExtracting = false
                            destroyExtractor()
                            playVideo(url)
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    Timber.d("WebView page loaded: $url")
                }
            }

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onM3u8Found(url: String) {
                    Timber.d("JS found m3u8: $url")
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

        view?.postDelayed({
            if (isExtracting) {
                isExtracting = false
                Timber.e("Extraction timeout, using fallback URL")
                viewModel.onExtractionFailed("Timeout")
                playVideo(FALLBACK_M3U8_URL)
            }
        }, 60000)
    }

    private fun injectExtractionScript() {
        val script = """
            (function() {
                function findM3U8() {
                    var result = null;
                    
                    // Pattern 1: Direct video element
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
                    
                    // Pattern 2: window.player object
                    if (!result && window.player) {
                        if (window.player.src && window.player.src.includes('.m3u8')) {
                            result = window.player.src;
                        } else if (window.player.sources && window.player.sources[0]) {
                            result = window.player.sources[0].src || window.player.sources[0].file;
                        } else if (window.player.currentSrc && window.player.currentSrc.includes('.m3u8')) {
                            result = window.player.currentSrc;
                        }
                    }
                    
                    // Pattern 3: HLS.js instances
                    if (!result) {
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
                    
                    // Pattern 4: data-src attributes
                    if (!result) {
                        var elements = document.querySelectorAll('[data-src]');
                        for (var i = 0; i < elements.length; i++) {
                            var ds = elements[i].getAttribute('data-src');
                            if (ds && ds.includes('.m3u8')) {
                                result = ds;
                                break;
                            }
                        }
                    }
                    
                    // Pattern 5: iframe src
                    if (!result) {
                        var iframes = document.querySelectorAll('iframe');
                        for (var i = 0; i < iframes.length; i++) {
                            var src = iframes[i].src;
                            if (src && src.includes('workers.dev')) {
                                result = src;
                                break;
                            }
                        }
                    }
                    
                    // Pattern 6: Scan scripts for m3u8 or workers.dev URLs
                    if (!result) {
                        var scripts = document.querySelectorAll('script');
                        for (var i = 0; i < scripts.length; i++) {
                            var content = scripts[i].textContent;
                            if (content) {
                                // Look for m3u8
                                var match = content.match(/https?:\/\/[^\s"'<>]+\.m3u8[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                                // Look for workers.dev URLs with m3u8
                                match = content.match(/https?:\/\/[^\s"'<>]*workers\.dev[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                                // Look for rainorbit or thunderleaf URLs
                                match = content.match(/https?:\/\/[^\s"'<>]*(?:rainorbit|thunderleaf)[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                            }
                        }
                    }
                    
                    return result;
                }
                
                var found = false;
                function tryFind() {
                    if (found) return;
                    var url = findM3U8();
                    if (url) {
                        found = true;
                        window.AndroidBridge.onM3u8Found(url);
                    } else {
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
                if (playbackState == Player.STATE_READY) {
                    logTrackInfo()
                }
            }
            
            override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                Timber.d("Tracks changed:")
                tracks.groups.forEachIndexed { index, trackGroup ->
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        Timber.d("  Track $index:$i - ${format.sampleMimeType}, ${format.width}x${format.height}, bitrate=${format.bitrate}")
                    }
                }
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
        isExtracting = false
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

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> Timber.d("Playback ended")
                    Player.STATE_READY -> Timber.d("Player ready")
                    Player.STATE_BUFFERING -> Timber.d("Player buffering")
                    Player.STATE_IDLE -> Timber.d("Player idle")
                }
            }
        })

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
        destroyExtractor()
        player?.release()
        player = null
        playerGlue = null
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroyView()
    }
}
