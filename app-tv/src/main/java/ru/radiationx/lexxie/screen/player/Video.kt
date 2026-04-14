package ru.radiationx.lexxie.screen.player

import ru.radiationx.data.entity.domain.release.PlayerSkips

data class Video(
    val url: String,
    val seek: Long,
    val title: String,
    val subtitle: String,
    val skips: PlayerSkips?,
)