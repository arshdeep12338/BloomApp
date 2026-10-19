package com.bloom.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bloom.app.di.AppContainer
import com.bloom.app.ui.home.HomeScreen
import com.bloom.app.ui.home.HomeViewModel
import com.bloom.app.ui.onboarding.OnboardingScreen
import com.bloom.app.ui.onboarding.OnboardingViewModel
import com.bloom.app.ui.splash.SplashScreen
import com.bloom.app.ui.splash.SplashViewModel
import com.bloom.app.ui.splash.StartDestination

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

@Composable
fun BloomNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            val viewModel: SplashViewModel = viewModel(
                factory = SplashViewModel.factory(container.onboardingRepository),
            )
            SplashScreen(
                viewModel = viewModel,
                appOpenAdManager = container.appOpenAdManager,
                onFinished = { destination ->
                    val route = when (destination) {
                        StartDestination.Onboarding -> Routes.ONBOARDING
                        StartDestination.Home -> Routes.HOME
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.factory(container.onboardingRepository),
            )
            OnboardingScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            // From here on the user is past splash/onboarding, so App Open Ads may be shown
            // when the app returns from the background.
            LaunchedEffect(Unit) { container.appOpenAdManager.foregroundAdsEnabled = true }

            val viewModel: HomeViewModel = viewModel()
            HomeScreen(viewModel = viewModel, analytics = container.adAnalytics)
        }
    }
}
