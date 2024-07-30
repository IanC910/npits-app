package com.passer.passwatch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
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
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            NearPassDatabase::class.java,
            "nearpass.db"
        ).build()
    }

    private val viewModel by viewModels<NearPassViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NearPassViewModel(db.dao) as T
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(dataStore)

        setContent {
            PassWatchTheme {
                val state by viewModel.state.collectAsState()

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
                        RidesScreen(state = state, onEvent = viewModel::onEvent)
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