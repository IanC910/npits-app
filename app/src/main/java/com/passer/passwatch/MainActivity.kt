package com.passer.passwatch

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.passer.passwatch.core.MainMenuScreen
import com.passer.passwatch.core.MapScreen
import com.passer.passwatch.core.NearPassScreen
import com.passer.passwatch.core.NewRideScreen
import com.passer.passwatch.core.RidesScreen
import com.passer.passwatch.core.SettingsScreen
import com.passer.passwatch.core.repo.NearPassDatabase
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.map.MapScreen
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassViewModel
import com.passer.passwatch.nearpass.presentation.NearPassScreen
import com.passer.passwatch.newride.NewRideScreen
import com.passer.passwatch.ride.domain.RideViewModel
import com.passer.passwatch.ride.presentation.RidesScreen
import com.passer.passwatch.settings.domain.SettingsViewModel
import com.passer.passwatch.settings.presentation.SettingsScreen
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

    private val nearPassViewModel by viewModels<NearPassViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NearPassViewModel(db.nearPassDao) as T
                }
            }
        }
    )

    private val rideViewModel by viewModels<RideViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RideViewModel(db.rideDao) as T
                }
            }
        }
    )

    private val settingsViewModel by viewModels<SettingsViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(userRepo) as T
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(dataStore)

        setContent {
            PassWatchTheme {
                val nearPassState by nearPassViewModel.state.collectAsState()
                val rideState by rideViewModel.state.collectAsState()
                val settingsState by settingsViewModel.state.collectAsState()

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
                        RidesScreen(
                            navController = navController,
                            state = rideState,
                            onEvent = rideViewModel::onEvent
                        )
                    }

                    composable<MapScreen> {
                        MapScreen()
                    }

                    composable<SettingsScreen> {
                        SettingsScreen(
                            state = settingsState,
                            onEvent = settingsViewModel::onEvent
                        )
                    }

                    composable<NearPassScreen> {
                        if (it.arguments == null) {
                            Log.e("NearPassScreen", "arguments is null")
                            return@composable
                        }

                        if (!it.arguments!!.containsKey("rideId")) {
                            Log.e("NearPassScreen", "rideId is null")
                            return@composable
                        }

                        nearPassViewModel.onEvent(
                            NearPassEvent.SetRideId(it.arguments!!.getInt("rideId"))
                        )

                        NearPassScreen(
                            state = nearPassState,
                            onEvent = nearPassViewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}