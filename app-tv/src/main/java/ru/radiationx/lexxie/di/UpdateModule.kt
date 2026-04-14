package ru.radiationx.lexxie.di

import ru.radiationx.lexxie.screen.update.UpdateController
import ru.radiationx.quill.QuillModule

class UpdateModule : QuillModule() {

    init {
        single<UpdateController>()
    }
}