package org.fnives.test.showcase.ui.compose.screen

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.ui.compose.screen.auth.AuthScreen
import org.fnives.test.showcase.ui.compose.screen.splash.SplashScreen
import org.koin.androidx.compose.get

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val isUserLogeInUseCase = get<IsUserLoggedInUseCase>()
    LaunchedEffect(isUserLogeInUseCase) {
        delay(500)
        navController.navigate(if (isUserLogeInUseCase.invoke()) "Home" else "Auth")
    }

    NavHost(navController, startDestination = "Splash", modifier = Modifier.background(MaterialTheme.colors.surface)) {
        composable("Splash") { SplashScreen() }
        composable("Auth") { AuthScreen() }
        composable("Home") { Text("Home") }
    }
}