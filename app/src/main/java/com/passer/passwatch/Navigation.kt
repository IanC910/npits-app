package com.passer.passwatch

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passer.passwatch.screen.MainScreen
import com.passer.passwatch.screen.MapScreen
import com.passer.passwatch.screen.NewRideScreen
import com.passer.passwatch.screen.RidesScreen
import com.passer.passwatch.screen.Screen
import com.passer.passwatch.screen.SettingsScreen

@Composable

fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainMenuScreen.route) {
        composable(route = Screen.MainMenuScreen.route) {
            MainScreen(navController = navController)
        }

        composable(
            route = Screen.NewRideScreen.route
        ) {
            NewRideScreen()
        }

        composable(
            route = Screen.RidesScreen.route
        ) {
            RidesScreen()
        }

        composable(
            route = Screen.MapScreen.route
        ) {
            MapScreen()
        }

        composable(
            route = Screen.SettingsScreen.route
        ) {
            SettingsScreen()
        }
    }
}


