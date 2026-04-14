package ru.radiationx.lexxie.di

import ru.radiationx.lexxie.screen.player.PlayerController
import ru.radiationx.quill.QuillModule

class PlayerModule : QuillModule() {

    init {
        single<PlayerController>()
    }
}