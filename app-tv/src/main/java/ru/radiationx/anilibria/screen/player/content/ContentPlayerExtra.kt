package ru.radiationx.anilibria.screen.player.content

import ru.radiationx.quill.QuillExtra

data class ContentPlayerExtra(
    val contentId: Int,
    val contentType: PlayerContentType,
    val season: Int = 1,
    val episode: Int = 1
) : QuillExtra
