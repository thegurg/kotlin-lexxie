package ru.radiationx.lexxie.screen.player.content

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.radiationx.lexxie.common.fragment.GuidedRouter
import ru.radiationx.lexxie.screen.ContentEpisodesScreen
import ru.radiationx.lexxie.screen.LifecycleViewModel
import ru.radiationx.lexxie.screen.ContentPlayerScreen
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.video.ContentType
import ru.radiationx.data.repository.VideoSourceRepository
import ru.radiationx.shared.ktx.EventFlow
import timber.log.Timber
import javax.inject.Inject

class ContentPlayerViewModel @Inject constructor(
    private val argExtra: ContentPlayerExtra,
    private val videoSourceRepository: VideoSourceRepository,
    private val guidedRouter: GuidedRouter,
    private val router: com.github.terrakok.cicerone.Router,
) : LifecycleViewModel() {

    val videoUrl = MutableStateFlow<String?>(null)
    val embedUrl = MutableStateFlow<String?>(null)
    val extractionState = MutableStateFlow<VideoExtractorBridge.ExtractionState>(VideoExtractorBridge.ExtractionState.Idle)
    val qualityState = MutableStateFlow<PlayerQuality?>(null)
    val speedState = MutableStateFlow<Float?>(null)
    val playAction = EventFlow<Boolean>()
    val errorAction = EventFlow<String>()

    val title: String
        get() = when (argExtra.contentType) {
            PlayerContentType.MOVIE -> "Фильм"
            PlayerContentType.TV_SERIES -> "Сезон ${argExtra.season}, Эпизод ${argExtra.episode}"
        }

    val subtitle: String
        get() = ""

    val isTvSeries: Boolean
        get() = argExtra.contentType == PlayerContentType.TV_SERIES

    val currentSeason: Int
        get() = argExtra.season

    val currentEpisode: Int
        get() = argExtra.episode

    private var currentQuality: PlayerQuality = PlayerQuality.HD

    init {
        loadVideo()
    }

    private fun loadVideo() {
        viewModelScope.launch {
            try {
                val type = when (argExtra.contentType) {
                    PlayerContentType.MOVIE -> ContentType.MOVIE
                    PlayerContentType.TV_SERIES -> ContentType.TV
                }

                val season = if (type == ContentType.TV) argExtra.season else null
                val episode = if (type == ContentType.TV) argExtra.episode else null

                val result = videoSourceRepository.getStreamInfo(
                    tmdbId = argExtra.contentId.toString(),
                    type = type,
                    season = season,
                    episode = episode
                )

                result.onSuccess { streamInfo ->
                    if (streamInfo.m3u8Url != null) {
                        videoUrl.value = streamInfo.m3u8Url
                        Timber.d("Using direct m3u8 URL")
                    } else if (streamInfo.embedUrl.isNotEmpty()) {
                        embedUrl.value = streamInfo.embedUrl
                        Timber.d("Embed URL available for extraction: ${streamInfo.embedUrl}")
                    } else {
                        Timber.e("No URL available from video sources")
                        errorAction.emit("Не удалось получить ссылку на видео")
                    }
                }.onFailure { error ->
                    Timber.e(error, "Failed to get stream URL")
                    errorAction.emit(error.message ?: "Ошибка загрузки видео")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading video")
                errorAction.emit(e.message ?: "Ошибка загрузки видео")
            }
        }
    }

    fun onExtractedUrl(m3u8Url: String) {
        Timber.d("Extracted m3u8 URL: $m3u8Url")
        videoUrl.value = m3u8Url
    }

    fun onExtractionFailed(error: String) {
        Timber.e("Extraction failed: $error")
        errorAction.emit("Не удалось извлечь видеопоток: $error")
    }

    fun onPauseClick(position: Long) {
    }

    fun onPrevClick() {
        if (argExtra.contentType == PlayerContentType.TV_SERIES) {
            var newSeason = argExtra.season
            var newEpisode = argExtra.episode - 1
            
            if (newEpisode < 1) {
                newSeason--
                newEpisode = 1
            }
            
            if (newSeason < 1) {
                newSeason = 1
                newEpisode = 1
            }
            
            router.navigateTo(ContentPlayerScreen(
                tmdbId = argExtra.contentId,
                isMovie = false,
                season = newSeason,
                episode = newEpisode
            ))
        }
    }

    fun onNextClick() {
        if (argExtra.contentType == PlayerContentType.TV_SERIES) {
            router.navigateTo(ContentPlayerScreen(
                tmdbId = argExtra.contentId,
                isMovie = false,
                season = argExtra.season,
                episode = argExtra.episode + 1
            ))
        }
    }

    fun onEpisodesClick() {
        guidedRouter.open(ContentEpisodesScreen(
            tmdbId = argExtra.contentId,
            seasonNumber = argExtra.season,
            episodeNumber = argExtra.episode
        ))
    }

    fun onQualityClick() {
    }

    fun onSpeedClick() {
    }

    fun onComplete() {
        if (argExtra.contentType == PlayerContentType.TV_SERIES) {
            onNextClick()
        }
    }

    fun onPrepare(duration: Long) {
        playAction.emit(true)
    }
}
