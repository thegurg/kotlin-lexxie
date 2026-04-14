package ru.radiationx.lexxie.screen.auth.main

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.lexxie.common.fragment.GuidedRouter
import ru.radiationx.lexxie.screen.AuthCredentialsGuidedScreen
import ru.radiationx.lexxie.screen.AuthOtpGuidedScreen
import ru.radiationx.lexxie.screen.LifecycleViewModel
import ru.radiationx.data.repository.AuthRepository
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    fun onCodeClick() {
        guidedRouter.open(AuthOtpGuidedScreen())
    }

    fun onClassicClick() {
        guidedRouter.open(AuthCredentialsGuidedScreen())
    }

    fun onSocialClick() {

    }

    fun onSkipClick() {
        viewModelScope.launch {
            authRepository.setAuthSkipped(true)
            guidedRouter.finishGuidedChain()
        }
    }
}