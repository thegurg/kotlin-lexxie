package ru.radiationx.anilibria.screen.player.webview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Button
import ru.radiationx.anilibria.R

class PlayerControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface ControlsListener {
        fun onPlayPauseClicked()
        fun onRewindClicked()
        fun onForwardClicked()
        fun onSeekTo(progress: Int)
        fun onPrevEpisodeClicked()
        fun onNextEpisodeClicked()
        fun onEpisodesClicked()
        fun onCloseClicked()
        fun onControlsVisibilityChanged(visible: Boolean)
    }

    private var listener: ControlsListener? = null
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideControls() }
    private var controlsVisible = true
    private var isPlaying = false

    private val btnRewind: ImageButton
    private val btnPlayPause: ImageButton
    private val btnForward: ImageButton
    private val btnPrevEpisode: Button
    private val btnEpisodes: Button
    private val btnNextEpisode: Button
    private val btnClose: Button
    private val seekBar: SeekBar
    private val tvTitle: TextView
    private val tvSubtitle: TextView
    private val tvCurrentTime: TextView
    private val tvDuration: TextView

    init {
        View.inflate(context, R.layout.player_controls_layout, this)
        
        btnRewind = findViewById(R.id.btnRewind)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnForward = findViewById(R.id.btnForward)
        btnPrevEpisode = findViewById(R.id.btnPrevEpisode)
        btnEpisodes = findViewById(R.id.btnEpisodes)
        btnNextEpisode = findViewById(R.id.btnNextEpisode)
        btnClose = findViewById(R.id.btnClose)
        seekBar = findViewById(R.id.seekBar)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvDuration = findViewById(R.id.tvDuration)

        setupClickListeners()
        setupSeekBar()
        startHideTimer()
    }

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            listener?.onPlayPauseClicked()
            resetHideTimer()
        }

        btnRewind.setOnClickListener {
            listener?.onRewindClicked()
            resetHideTimer()
        }

        btnForward.setOnClickListener {
            listener?.onForwardClicked()
            resetHideTimer()
        }

        btnPrevEpisode.setOnClickListener {
            listener?.onPrevEpisodeClicked()
            resetHideTimer()
        }

        btnEpisodes.setOnClickListener {
            listener?.onEpisodesClicked()
            resetHideTimer()
        }

        btnNextEpisode.setOnClickListener {
            listener?.onNextEpisodeClicked()
            resetHideTimer()
        }

        btnClose.setOnClickListener {
            listener?.onCloseClicked()
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    listener?.onSeekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                cancelHideTimer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                resetHideTimer()
            }
        })
    }

    fun setControlsListener(listener: ControlsListener) {
        this.listener = listener
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }

    fun setSubtitle(subtitle: String) {
        tvSubtitle.text = subtitle
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        btnPlayPause.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)
    }

    fun updateProgress(currentMs: Long, durationMs: Long) {
        if (durationMs > 0) {
            val progress = ((currentMs.toFloat() / durationMs) * 100).toInt()
            seekBar.progress = progress
            tvCurrentTime.text = formatTime(currentMs)
            tvDuration.text = formatTime(durationMs)
        }
    }

    fun setEpisodeButtonsVisible(visible: Boolean) {
        btnPrevEpisode.visibility = if (visible) View.VISIBLE else View.GONE
        btnEpisodes.visibility = if (visible) View.VISIBLE else View.GONE
        btnNextEpisode.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun showControls() {
        visibility = View.VISIBLE
        controlsVisible = true
        resetHideTimer()
        listener?.onControlsVisibilityChanged(true)
    }

    fun hideControls() {
        visibility = View.GONE
        controlsVisible = false
        cancelHideTimer()
        listener?.onControlsVisibilityChanged(false)
    }

    fun toggleControls() {
        if (controlsVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    fun areControlsVisible(): Boolean = controlsVisible

    private fun startHideTimer() {
        cancelHideTimer()
        hideHandler.postDelayed(hideRunnable, 5000)
    }

    private fun resetHideTimer() {
        if (controlsVisible) {
            startHideTimer()
        }
    }

    private fun cancelHideTimer() {
        hideHandler.removeCallbacks(hideRunnable)
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = ms / (1000 * 60 * 60)
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun setNextButtonEnabled(enabled: Boolean) {
        btnNextEpisode.isEnabled = enabled
        btnNextEpisode.alpha = if (enabled) 1f else 0.5f
    }

    fun setPrevButtonEnabled(enabled: Boolean) {
        btnPrevEpisode.isEnabled = enabled
        btnPrevEpisode.alpha = if (enabled) 1f else 0.5f
    }
}
