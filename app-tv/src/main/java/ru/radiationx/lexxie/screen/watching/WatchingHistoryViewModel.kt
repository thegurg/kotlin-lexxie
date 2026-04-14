package ru.radiationx.lexxie.screen.watching

import ru.radiationx.lexxie.common.BaseCardsViewModel
import ru.radiationx.lexxie.common.CardsDataConverter
import ru.radiationx.lexxie.common.LibriaCard
import ru.radiationx.lexxie.common.LibriaCardRouter
import ru.radiationx.data.repository.HistoryRepository
import javax.inject.Inject

class WatchingHistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    override val defaultTitle: String = "История"

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = historyRepository
        .getReleases()
        .items
        .let { historyItems ->
            historyItems.map { converter.toCard(it) }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean =
        false

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}