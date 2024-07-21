package com.passer.passwatch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passer.passwatch.data.UserPreferencesRepository
import com.passer.passwatch.screen.MainMenuScreen
import com.passer.passwatch.screen.MainScreen
import com.passer.passwatch.screen.MapScreen
import com.passer.passwatch.screen.NewRideScreen
import com.passer.passwatch.screen.RidesScreen
import com.passer.passwatch.screen.SettingsScreen
import com.passer.passwatch.ui.theme.PassWatchTheme

private const val PREFERENCE_NAME = "user_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

val MAC_KEY = stringPreferencesKey("hub_mac_address")


class MainActivity : ComponentActivity() {
    lateinit var userRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(dataStore)

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
                        SettingsScreen(userRepo)
                    }
                }
            }
        }
    }
}