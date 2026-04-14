package ru.radiationx.anilibria.screen.player.content

import ru.radiationx.quill.QuillExtra

data class ContentEpisodesExtra(
    val tmdbId: Int,
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1
) : QuillExtra
