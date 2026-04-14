package ru.radiationx.lexxie.screen.player.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber

class PlayerWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    interface PlayerListener {
        fun onPlayerReady()
        fun onPlayerError(message: String)
        fun onPlayPauseClicked()
        fun onSeekTo(position: Int)
    }

    private var listener: PlayerListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPlayerReady = false

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    fun initialize() {
        settings.apply {
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

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Timber.d("PlayerWebView page loaded: $url")
                mainHandler.postDelayed({
                    injectPlayerControlScript()
                }, 2000)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }

        addJavascriptInterface(PlayerJsBridge(), "AndroidPlayer")
    }

    fun setPlayerListener(listener: PlayerListener) {
        this.listener = listener
    }

    fun loadEmbedUrl(url: String) {
        isPlayerReady = false
        Timber.d("PlayerWebView loading: $url")
        loadUrl(url)
    }

    private fun injectPlayerControlScript() {
        val script = """
            (function() {
                function findVideoAndSetup() {
                    var video = document.querySelector('video');
                    if (!video) {
                        video = document.querySelector('iframe');
                        if (video) {
                            var iframeDoc = video.contentDocument || video.contentWindow.document;
                            video = iframeDoc ? iframeDoc.querySelector('video') : null;
                        }
                    }
                    
                    if (video) {
                        AndroidPlayer.onPlayerReady();
                        
                        video.addEventListener('play', function() {
                            AndroidPlayer.onPlayStateChanged(true);
                        });
                        
                        video.addEventListener('pause', function() {
                            AndroidPlayer.onPlayStateChanged(false);
                        });
                        
                        video.addEventListener('ended', function() {
                            AndroidPlayer.onPlaybackEnded();
                        });
                        
                        video.addEventListener('error', function(e) {
                            AndroidPlayer.onPlayerError('Video error: ' + (e.message || 'Unknown'));
                        });
                        
                        return true;
                    }
                    return false;
                }
                
                function tryInject() {
                    if (findVideoAndSetup()) {
                        return;
                    }
                    setTimeout(tryInject, 500);
                }
                
                tryInject();
            })();
        """.trimIndent()

        evaluateJavascript(script, null)
    }

    fun simulatePlayPause() {
        val script = """
            (function() {
                var video = document.querySelector('video');
                if (video) {
                    if (video.paused) {
                        video.play();
                    } else {
                        video.pause();
                    }
                    AndroidPlayer.onPlayStateChanged(!video.paused);
                }
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    fun seekForward(seconds: Int = 10) {
        val script = """
            (function() {
                var video = document.querySelector('video');
                if (video) {
                    video.currentTime += $seconds;
                    AndroidPlayer.onSeekCompleted(video.currentTime);
                }
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    fun seekBackward(seconds: Int = 10) {
        val script = """
            (function() {
                var video = document.querySelector('video');
                if (video) {
                    video.currentTime = Math.max(0, video.currentTime - $seconds);
                    AndroidPlayer.onSeekCompleted(video.currentTime);
                }
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    fun seekTo(position: Int) {
        val script = """
            (function() {
                var video = document.querySelector('video');
                if (video && video.duration) {
                    video.currentTime = ($position / 100) * video.duration;
                    AndroidPlayer.onSeekCompleted(video.currentTime);
                }
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    fun getCurrentTime(): Int {
        return 0
    }

    fun getDuration(): Int {
        return 0
    }

    fun cleanup() {
        stopLoading()
        clearCache(true)
        clearHistory()
        removeJavascriptInterface("AndroidPlayer")
    }

    private inner class PlayerJsBridge {
        @JavascriptInterface
        fun onPlayerReady() {
            mainHandler.post {
                isPlayerReady = true
                listener?.onPlayerReady()
            }
        }

        @JavascriptInterface
        fun onPlayerError(message: String) {
            mainHandler.post {
                listener?.onPlayerError(message)
            }
        }

        @JavascriptInterface
        fun onPlayStateChanged(isPlaying: Boolean) {
            mainHandler.post {
                Timber.d("Play state changed: isPlaying=$isPlaying")
            }
        }

        @JavascriptInterface
        fun onPlaybackEnded() {
            mainHandler.post {
                Timber.d("Playback ended")
            }
        }

        @JavascriptInterface
        fun onSeekCompleted(position: Double) {
            mainHandler.post {
                Timber.d("Seek completed: $position")
            }
        }
    }
}
