package ru.radiationx.data.di.providers

import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.Client
import javax.inject.Inject

class TmdbNetworkClient @Inject constructor(
    clientWrapper: TmdbClientWrapper,
    sharedBuildConfig: SharedBuildConfig,
) : Client(clientWrapper, sharedBuildConfig)
