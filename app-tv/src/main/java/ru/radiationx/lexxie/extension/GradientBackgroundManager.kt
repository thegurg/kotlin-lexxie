package ru.radiationx.lexxie.extension

import ru.radiationx.lexxie.common.GradientBackgroundManager
import ru.radiationx.lexxie.common.LibriaCard
import ru.radiationx.lexxie.common.LinkCard
import ru.radiationx.lexxie.common.LoadingCard

fun GradientBackgroundManager.applyCard(card: Any?) = when (card) {
    is LibriaCard -> applyImage(card.image)
    is LinkCard -> {
    }
    is LoadingCard -> {
    }
    null -> {
    }
    else -> clearGradient()
}