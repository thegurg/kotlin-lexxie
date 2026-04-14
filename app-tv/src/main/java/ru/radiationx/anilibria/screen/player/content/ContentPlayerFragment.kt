package ru.radiationx.anilibria.screen.player.content

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
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
        
        private const val TEST_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

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

    @Suppress("DEPRECATION")
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        Timber.d("ContentPlayerFragment onViewCreated - SIMPLE EXOPLAYER TEST")
        
        initializePlayer()
        initializeRows()
        
        // Load test video
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(TEST_URL)))
        player?.prepare()
        player?.play()
        
        Timber.d("ExoPlayer started playing test video")
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
        player?.release()
        player = null
        playerGlue = null
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDestroyView()
    }
}
