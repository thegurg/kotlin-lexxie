# WebView/API Sniffing - Извлечение m3u8 потоков

## Что такое m3u8/HLS?

HLS (HTTP Live Streaming) - протокол потоковой передачи видео.

- `.m3u8` = плейлист (список видео сегментов)
- `.ts` = видео сегменты (фрагменты)
- ExoPlayer/ffmpeg умеют воспроизводить напрямую

## Принцип работы

```
Приложение/Браузер запрашивает контент
        ↓
Сервер/CDN возвращает плейлист с ссылками на видео
        ↓
Sniffer перехватывает этот запрос
        ↓
Извлекается прямая ссылка на m3u8
        ↓
Плеер воспроизводит m3u8 напрямую
```

```
Input:  embed_url → https://site.com/player?id=123
Output: m3u8_url → https://cdn.com/stream/playlist.m3u8?token=abc

ExoPlayer/ffplay могут играть напрямую:
ffplay "https://cdn.com/stream/playlist.m3u8"
```

## Где применяется

| Область | Примеры |
|---------|---------|
| Cybersecurity | Анализ трафика, pentesting, аудит безопасности |
| Terminal | CLI утилиты для извлечения потоков, автоматизация |
| Desktop | Electron/Qt приложения с WebView |
| Mobile | Android WebView, iOS WKWebView |
| Automation | CI/CD пайплайны для стриминга |

## Методы перехвата

### 1. shouldInterceptRequest (Android)

```kotlin
// Android WebView - перехват сетевых запросов
webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString()
        
        // Ищем m3u8 в URL запроса
        if (url?.contains(".m3u8") == true) {
            // Найден m3u8 поток!
            onStreamFound(url)
        }
        
        return super.shouldInterceptRequest(view, request)
    }
}
```

### 2. WKWebView (iOS)

```swift
// iOS WKWebView
class WebViewProxy: NSObject, WKNavigationDelegate {
    func webView(_ webView: WKWebView, 
                 decidePolicyFor navigationAction: WKNavigationAction,
                 decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        
        if let url = navigationAction.request.url,
           url.absoluteString.contains(".m3u8") {
            // Найден m3u8 поток
            onStreamFound(url.absoluteString)
        }
        
        decisionHandler(.allow)
    }
}
```

### 3. Electron / WebView2 (Desktop)

```javascript
// Electron - перехват через session
session.webRequest.onBeforeRequest({}, (details) => {
    if (details.url.includes('.m3u8')) {
        console.log('Found stream:', details.url)
        // Извлекаем URL
    }
})

// WebView2 (Windows)
webView.CoreWebView2.WebResourceRequested += (sender, args) => {
    var url = args.Request.Uri;
    if (url.Contains(".m3u8")) {
        // Обработка m3u8
    }
}
```

### 4. JavaScript Injection

```javascript
// Внедряется в страницу через evaluateJavascript()

// Method 1: performance.getEntries() - загруженные ресурсы
var entries = performance.getEntriesByType('resource');
for (var i = 0; i < entries.length; i++) {
    var name = entries[i].name || entries[i].url || '';
    if (name.includes('.m3u8')) {
        return name;  // Найдено!
    }
}

// Method 2: video элементы
var videos = document.querySelectorAll('video');
for (var v = 0; v < videos.length; v++) {
    if (videos[v].src.includes('.m3u8')) {
        return videos[v].src;
    }
}

// Method 3: window.player объект
if (window.player && window.player.src) {
    return window.player.src;
}

// Method 4: HLS.js instances
for (var key in window) {
    if (window[key] && typeof window[key].loadSource === 'function') {
        return window[key].url || window[key]._url;
    }
}

// Method 5: data-src атрибуты
var elements = document.querySelectorAll('[data-src]');
for (var i = 0; i < elements.length; i++) {
    var src = elements[i].getAttribute('data-src');
    if (src && src.includes('.m3u8')) {
        return src;
    }
}

// Method 6: iframe src
var iframes = document.querySelectorAll('iframe');
for (var i = 0; i < iframes.length; i++) {
    if (iframes[i].src.includes('.m3u8')) {
        return iframes[i].src;
    }
}

// Отправка в нативный код
AndroidBridge.onM3u8Found(result);
```

### 5. Network Sniffing (Terminal)

```bash
# mitmproxy - перехват HTTP/HTTPS
mitmproxy --filter ".m3u8"

# tshark - анализ трафика
tshark -Y "http.request.uri contains m3u8"

# tcpdump + grep
tcpdump -i any -A | grep "\.m3u8"

# Charles Proxy - GUI для анализа
# Настроить фильтр: .m3u8
```

### 6. Proxies / MitM

```python
# Python прокси для перехвата
class ProxyMiddleware:
    def process_response(self, request, response):
        if '.m3u8' in request.url:
            print(f"Found: {request.url}")
            # Сохраняем URL
        return response
```

## Инструменты

| Тип | Инструменты |
|------|------------|
| Mobile Android | WebView.shouldInterceptRequest, Proxydroid |
| Mobile iOS | WKWebView, NSURLProtocol |
| Desktop Electron | session.webRequest API |
| Desktop Native | Charles, mitmproxy, Fiddler |
| Terminal | curl, wget, ffmpeg, yt-dlp, streamlink |
| CI/CD | yt-dlp, gallery-dl, streamlink |
| Browser | Browser DevTools, Network tab |

## Ограничения

| Ограничение | Описание |
|------------|---------|
| HTTPS Pinning | SSL pinning блокирует MitM |
| Шифрованные потоки | Нужен дешифратор для DRM |
| DRM контент | Widevine/PlayReady/FairPlay требуют лицензию |
| Временные токены | URL могут содержать одноразовые токены |
| CORS | CDN должен разрешать запросы с нашего домена |

## Пример: Android WebView Sniffer (минимальный)

```kotlin
class VideoSniffer(private val context: Context) {
    
    private var webView: WebView? = null
    private var onM3u8Found: ((String) -> Unit)? = null
    
    fun start(url: String, callback: (String) -> Unit) {
        onM3u8Found = callback
        
        webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
            
            webChromeClient = WebChromeClient()
            
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val requestUrl = request?.url?.toString()
                    
                    if (requestUrl?.contains(".m3u8") == true) {
                        // Найден m3u8 поток
                        callback(requestUrl)
                        stop()
                    }
                    
                    return super.shouldInterceptRequest(view, request)
                }
            }
            
            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(1, 1)
            
            loadUrl(url)
        }
    }
    
    fun stop() {
        webView?.destroy()
        webView = null
    }
}

// Использование:
val sniffer = VideoSniffer(context)
sniffer.start("https://site.com/embed/video/123") { m3u8Url ->
    // Воспроизводим m3u8
    exoPlayer.setMediaItem(MediaItem.fromUri(m3u8Url))
    exoPlayer.prepare()
    exoPlayer.play()
}
```

## Пример: Electron (Desktop)

```javascript
const { session } = require('electron');

// Перехват всех запросов
session.defaultSession.webRequest.onBeforeRequest((details, callback) => {
    if (details.url.includes('.m3u8')) {
        console.log('Found stream URL:', details.url);
        // Сохраняем или воспроизводим
    }
    callback({ cancel: false });
});

// Воспроизведение через ffplay
const { exec } = require('child_process');
function playStream(url) {
    exec(`ffplay -autoexit "${url}"`);
}
```

## Безопасность и легальное использование

### Легально:
- Анализ своего собственного трафика
- Тестирование своих приложений
- Аудит безопасности с разрешения
- Образовательные цели

### Нелегально:
- Обход DRM/пиратство
- Несанкционированный перехват чужого трафика
- Нарушение Terms of Service

### Рекомендации:
1. Используйте только для легальных целей
2. Не распространяйте извлеченные URL
3. Уважайте авторские права
4. Тестируйте на собственном контенте

## Дополнительные ресурсы

- [HLS Specification](https://tools.ietf.org/html/rfc8216)
- [ExoPlayer Documentation](https://exoplayer.dev/)
- [ffmpeg Documentation](https://ffmpeg.org/documentation.html)
- [mitmproxy Documentation](https://docs.mitmproxy.org/)
