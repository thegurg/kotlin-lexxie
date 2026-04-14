package ru.radiationx.lexxie.screen.player.content

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class VideoExtractorBridge {

    interface ExtractionListener {
        fun onExtractionSuccess(m3u8Url: String)
        fun onExtractionFailed(error: String)
    }

    sealed class ExtractionState {
        object Idle : ExtractionState()
        object Loading : ExtractionState()
        data class Success(val m3u8Url: String) : ExtractionState()
        data class Error(val message: String) : ExtractionState()
    }

    private val _extractionState = MutableStateFlow<ExtractionState>(ExtractionState.Idle)
    val extractionState: StateFlow<ExtractionState> = _extractionState.asStateFlow()

    private var webView: WebView? = null
    private var listener: ExtractionListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private val m3u8UrlRef = AtomicReference<String?>(null)
    private var timeoutLatch: CountDownLatch? = null
    private var isExtracting = false

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(context: Context): WebView {
        val wv = WebView(context)
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
            userAgentString = "Mozilla/5.0 (Linux; Android 11; Build/RKQ1.200826.002) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
        }
        
        webView = wv
        return wv
    }

    fun extractStreamUrl(embedUrl: String, listener: ExtractionListener, timeoutMs: Long = 30000): Boolean {
        if (isExtracting) {
            Timber.d("Already extracting, skipping")
            return false
        }
        
        this.listener = listener
        _extractionState.value = ExtractionState.Loading
        m3u8UrlRef.set(null)
        isExtracting = true
        
        webView?.apply {
            stopLoading()
            clearCache(true)
            clearHistory()
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress >= 100) {
                        mainHandler.postDelayed({
                            injectExtractionScript()
                        }, 3000)
                    }
                }
            }
            
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val url = request?.url?.toString()
                    
                    if (url != null && url.contains(".m3u8") && !url.contains(".m3u8?")) {
                        Timber.d("Intercepted m3u8: ${url.take(80)}")
                        if (m3u8UrlRef.compareAndSet(null, url)) {
                            mainHandler.post {
                                _extractionState.value = ExtractionState.Success(url)
                                listener.onExtractionSuccess(url)
                            }
                            timeoutLatch?.countDown()
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Timber.d("Page finished: $url")
                }
            }
            
            loadUrl(embedUrl)
        }
        
        Thread {
            timeoutLatch = CountDownLatch(1)
            val awaited = timeoutLatch?.await(timeoutMs, TimeUnit.MILLISECONDS) ?: false
            if (!awaited && m3u8UrlRef.get() == null) {
                mainHandler.post {
                    isExtracting = false
                    val errorMsg = "Timeout waiting for m3u8 stream"
                    Timber.e(errorMsg)
                    _extractionState.value = ExtractionState.Error(errorMsg)
                    listener.onExtractionFailed(errorMsg)
                }
            }
        }.start()
        
        return true
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
                        // Check source elements
                        var sources = videos[v].querySelectorAll('source');
                        for (var s = 0; s < sources.length; s++) {
                            if (sources[s].src && sources[s].src.includes('.m3u8')) {
                                result = sources[s].src;
                                break;
                            }
                        }
                    }
                    
                    // Pattern 2: Check window objects
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
                    if (!result && window.hlsjs) {
                        for (var key in window.hlsjs) {
                            if (window.hlsjs[key].url && window.hlsjs[key].url.includes('.m3u8')) {
                                result = window.hlsjs[key].url;
                                break;
                            }
                        }
                    }
                    
                    // Pattern 4: Look for data attributes
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
                    
                    // Pattern 5: Scan script tags for m3u8 URLs
                    if (!result) {
                        var scripts = document.querySelectorAll('script');
                        for (var i = 0; i < scripts.length; i++) {
                            var content = scripts[i].textContent;
                            if (content) {
                                var match = content.match(/https?:\/\/[^\s"'<>]+\.m3u8[^\s"'<>]*/);
                                if (match) {
                                    result = match[0];
                                    break;
                                }
                            }
                        }
                    }
                    
                    return result;
                }
                
                function tryFind() {
                    var url = findM3U8();
                    if (url) {
                        Timber.d('Found m3u8 via JS: ' + url);
                        window.AndroidBridge.onM3u8Found(url);
                    } else {
                        setTimeout(tryFind, 1000);
                    }
                }
                
                tryFind();
            })();
        """.trimIndent()

        try {
            webView?.evaluateJavascript(script, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to inject script")
        }
    }

    fun destroy() {
        isExtracting = false
        webView?.apply {
            stopLoading()
            clearCache(true)
            clearHistory()
            removeJavascriptInterface("AndroidBridge")
            destroy()
        }
        webView = null
        listener = null
        timeoutLatch?.countDown()
    }
}
