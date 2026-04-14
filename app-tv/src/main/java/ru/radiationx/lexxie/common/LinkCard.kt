package ru.radiationx.lexxie.common

data class LinkCard(
    val title: String
) : CardItem {
    override fun getId(): Int {
        return title.hashCode()
    }
}