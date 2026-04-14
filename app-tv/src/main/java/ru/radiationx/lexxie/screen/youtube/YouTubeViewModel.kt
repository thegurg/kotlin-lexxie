package ru.radiationx.lexxie.screen.youtube

import ru.radiationx.lexxie.common.BaseCardsViewModel
import ru.radiationx.lexxie.common.CardsDataConverter
import ru.radiationx.lexxie.common.LibriaCard
import ru.radiationx.lexxie.common.LibriaCardRouter
import ru.radiationx.data.repository.YoutubeRepository
import javax.inject.Inject

class YouTubeViewModel @Inject constructor(
    private val youtubeRepository: YoutubeRepository,
    private val converter: CardsDataConverter,
    private val cardRouter: LibriaCardRouter
) : BaseCardsViewModel() {

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = youtubeRepository
        .getYoutubeList(requestPage)
        .let { youtubeItems ->
            youtubeItems.data.map { converter.toCard(it) }
        }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}