package com.bloom.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bloom.app.data.OnboardingRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private var finishing = false

    /** Persists "onboarding done" (used by both Get Started and Skip), then navigates. */
    fun completeOnboarding(onDone: () -> Unit) {
        if (finishing) return // ignore double taps
        finishing = true
        viewModelScope.launch {
            onboardingRepository.setOnboardingCompleted()
            onDone()
        }
    }

    companion object {
        fun factory(repository: OnboardingRepository) = viewModelFactory {
            initializer { OnboardingViewModel(repository) }
        }
    }
}
