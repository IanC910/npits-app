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
import com.passer.passwatch.data.UserPreferencesRepository
import com.passer.passwatch.model.NearPassDatabase
import com.passer.passwatch.screen.MainMenuScreen
import com.passer.passwatch.screen.MainScreen
import com.passer.passwatch.screen.MapScreen
import com.passer.passwatch.screen.NearPassScreen
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(dataStore)

        setContent {
            PassWatchTheme {
                val nearPassState by nearPassViewModel.state.collectAsState()
                val rideState by rideViewModel.state.collectAsState()

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
                        SettingsScreen(userRepo)
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