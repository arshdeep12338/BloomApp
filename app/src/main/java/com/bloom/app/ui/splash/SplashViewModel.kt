package com.bloom.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bloom.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first

enum class StartDestination { Onboarding, Home }

class SplashViewModel(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    /** First launch → Onboarding, otherwise → Home. */
    suspend fun resolveStartDestination(): StartDestination =
        if (preferences.preferences.first().onboardingCompleted) {
            StartDestination.Home
        } else {
            StartDestination.Onboarding
        }

    companion object {
        fun factory(repository: UserPreferencesRepository) = viewModelFactory {
            initializer { SplashViewModel(repository) }
        }
    }
}
