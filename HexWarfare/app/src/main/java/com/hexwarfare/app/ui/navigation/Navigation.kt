package com.hexwarfare.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hexwarfare.app.ui.screens.MainMenuScreen
import com.hexwarfare.app.ui.screens.GameScreen

object Routes {
    const val MAIN_MENU = "main_menu"
    const val GAME = "game"
}

@Composable
fun HexWarfareNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_MENU
    ) {
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                onStartGame = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.MAIN_MENU) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.GAME) {
            GameScreen()
        }
    }
}
