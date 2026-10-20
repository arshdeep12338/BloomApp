package com.bloom.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bloom.app.di.AppContainer
import com.bloom.app.ui.main.MainScreen
import com.bloom.app.ui.main.MainViewModel
import com.bloom.app.ui.onboarding.OnboardingScreen
import com.bloom.app.ui.onboarding.OnboardingViewModel
import com.bloom.app.ui.splash.SplashScreen
import com.bloom.app.ui.splash.SplashViewModel
import com.bloom.app.ui.splash.StartDestination

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
}

@Composable
fun BloomNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            val viewModel: SplashViewModel = viewModel(
                factory = SplashViewModel.factory(container.userPreferences),
            )
            SplashScreen(
                viewModel = viewModel,
                appOpenAdManager = container.appOpenAdManager,
                onFinished = { destination ->
                    val route = when (destination) {
                        StartDestination.Onboarding -> Routes.ONBOARDING
                        StartDestination.Home -> Routes.MAIN
                    }
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.factory(container.userPreferences),
            )
            OnboardingScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.MAIN) {
            DisposableEffect(Unit) {
                container.appOpenAdManager.foregroundAdsEnabled = true
                onDispose { container.appOpenAdManager.foregroundAdsEnabled = false }
            }

            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.factory(container.userPreferences, container.wellness),
            )
            MainScreen(
                viewModel = viewModel,
                analytics = container.adAnalytics,
                onSignedOut = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
            )
        }
    }
}
