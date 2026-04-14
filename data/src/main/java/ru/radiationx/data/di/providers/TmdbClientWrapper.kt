package ru.radiationx.data.di.providers

import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

class TmdbClientWrapper @Inject constructor(
    provider: TmdbOkHttpProvider,
) : ClientWrapper(provider)
