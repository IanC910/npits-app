package com.passer.passwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passer.passwatch.screen.MainMenuScreen
import com.passer.passwatch.screen.MainScreen
import com.passer.passwatch.screen.MapScreen
import com.passer.passwatch.screen.NewRideScreen
import com.passer.passwatch.screen.RidesScreen
import com.passer.passwatch.screen.SettingsScreen
import com.passer.passwatch.ui.theme.PassWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassWatchTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = MainMenuScreen
                ) {
                    composable<MainMenuScreen> {
                        MainScreen(navController)
                    }

                    composable<NewRideScreen> {
                        NewRideScreen()
                    }

                    composable<RidesScreen> {
                        RidesScreen()
                    }

                    composable<MapScreen> {
                        MapScreen()
                    }

                    composable<SettingsScreen> {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}