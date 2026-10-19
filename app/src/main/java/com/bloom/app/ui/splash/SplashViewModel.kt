package com.bloom.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bloom.app.data.OnboardingRepository
import kotlinx.coroutines.flow.first

enum class StartDestination { Onboarding, Home }

class SplashViewModel(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    /** First launch → Onboarding, otherwise → Home. */
    suspend fun resolveStartDestination(): StartDestination =
        if (onboardingRepository.onboardingCompleted.first()) {
            StartDestination.Home
        } else {
            StartDestination.Onboarding
        }

    companion object {
        fun factory(repository: OnboardingRepository) = viewModelFactory {
            initializer { SplashViewModel(repository) }
        }
    }
}
