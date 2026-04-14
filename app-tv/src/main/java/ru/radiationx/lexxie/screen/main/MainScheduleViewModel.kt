package ru.radiationx.lexxie.screen.main

import com.github.terrakok.cicerone.Router
import ru.radiationx.lexxie.common.BaseCardsViewModel
import ru.radiationx.lexxie.common.CardsDataConverter
import ru.radiationx.lexxie.common.LibriaCard
import ru.radiationx.lexxie.common.LibriaCardRouter
import ru.radiationx.lexxie.common.LinkCard
import ru.radiationx.lexxie.screen.ScheduleScreen
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.ScheduleRepository
import ru.radiationx.shared.ktx.asDayNameDeclension
import ru.radiationx.shared.ktx.asDayPretext
import ru.radiationx.shared.ktx.asMsk
import ru.radiationx.shared.ktx.getDayOfWeek
import ru.radiationx.shared.ktx.isSameDay
import ru.radiationx.shared.ktx.lowercaseDefault
import java.util.Date
import javax.inject.Inject

class MainScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val releaseInteractor: ReleaseInteractor,
    private val converter: CardsDataConverter,
    private val router: Router,
    private val cardRouter: LibriaCardRouter,
) : BaseCardsViewModel() {

    override val defaultTitle: String = "Ожидается сегодня"

    override val loadMoreCard: LinkCard = LinkCard("Открыть полное расписание")

    override val preventClearOnRefresh: Boolean = true

    override fun onResume() {
        super.onResume()
        onRefreshClick()
    }

    override suspend fun getLoader(requestPage: Int): List<LibriaCard> = scheduleRepository
        .loadSchedule()
        .also { days ->
            val allReleases = days.map { day -> day.items.map { it.releaseItem } }.flatten()
            releaseInteractor.updateItemsCache(allReleases)
        }
        .let { schedueDays ->
            val currentTime = System.currentTimeMillis()
            val mskTime = System.currentTimeMillis().asMsk()

            val mskDay = mskTime.getDayOfWeek()


            val dayTitle = if (Date(currentTime).isSameDay(Date(mskTime))) {
                "Ожидается сегодня"
            } else {
                "Ожидается ${mskDay.asDayPretext()} ${
                    mskDay.asDayNameDeclension().lowercaseDefault()
                } (по МСК)"
            }
            rowTitle.value = dayTitle

            val items = schedueDays.firstOrNull { it.day == mskDay }?.items?.map { it.releaseItem }
                .orEmpty()

            items.map { converter.toCard(it) }
        }

    override fun hasMoreCards(newCards: List<LibriaCard>, allCards: List<LibriaCard>): Boolean {
        return true
    }

    override fun onLinkCardClick() {
        router.navigateTo(ScheduleScreen())
    }

    override fun onLinkCardBind() {
        // do nothing
    }

    override fun onLibriaCardClick(card: LibriaCard) {
        cardRouter.navigate(card)
    }
}