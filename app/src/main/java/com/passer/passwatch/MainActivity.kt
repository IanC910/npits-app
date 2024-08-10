package com.passer.passwatch

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.passer.passwatch.core.TelemetryScreen
import com.passer.passwatch.core.repo.NearPassDatabase
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.map.MapScreen
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassViewModel
import com.passer.passwatch.nearpass.presentation.NearPassScreen
import com.passer.passwatch.newride.domain.NewRideViewModel
import com.passer.passwatch.newride.presentation.NewRideScreen
import com.passer.passwatch.ride.domain.RideViewModel
import com.passer.passwatch.ride.presentation.RidesScreen
import com.passer.passwatch.settings.domain.SettingsViewModel
import com.passer.passwatch.settings.presentation.SettingsScreen
import com.passer.passwatch.telemetry.domain.TelemetryEvent
import com.passer.passwatch.telemetry.domain.TelemetryViewModel
import com.passer.passwatch.telemetry.presentation.TelemetryScreen
import com.passer.passwatch.ui.theme.PassWatchTheme

private const val PREFERENCE_NAME = "user_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

val MAC_KEY = stringPreferencesKey("hub_mac_address")


class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1
    }

    lateinit var userRepo: UserPreferencesRepository

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            NearPassDatabase::class.java,
            "nearpass.db"
        ).build()
    }

    @Suppress("UNCHECKED_CAST")
    private val newRideViewModel by viewModels<NewRideViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NewRideViewModel() as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val nearPassViewModel by viewModels<NearPassViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NearPassViewModel(db.nearPassDao) as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val rideViewModel by viewModels<RideViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RideViewModel(db.rideDao) as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val settingsViewModel by viewModels<SettingsViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val bluetoothManager =
                        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    return SettingsViewModel(userRepo, bluetoothManager) as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val telemetryViewModel by viewModels<TelemetryViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val bluetoothManager =
                        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    return TelemetryViewModel(applicationContext, bluetoothManager) as T
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepo = UserPreferencesRepository(dataStore)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting Bluetooth permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting Bluetooth permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting Bluetooth permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting Bluetooth permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting Bluetooth permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        setContent {
            PassWatchTheme {
                val newRideState by newRideViewModel.state.collectAsState()
                val nearPassState by nearPassViewModel.state.collectAsState()
                val rideState by rideViewModel.state.collectAsState()
                val settingsState by settingsViewModel.state.collectAsState()
                val telemetryState by telemetryViewModel.state.collectAsState()

                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = MainMenuScreen
                ) {
                    composable<MainMenuScreen> {
                        MainScreen(navController)
                    }

                    composable<NewRideScreen> {
                        NewRideScreen(newRideState, newRideViewModel::onEvent)
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
                            navController = navController,
                            state = settingsState,
                            onEvent = settingsViewModel::onEvent
                        )
                    }

                    composable<TelemetryScreen> {
                        if (it.arguments == null) {
                            Log.e("TelemetryScreen", "arguments is null")
                            return@composable
                        }

                        if (!it.arguments!!.containsKey("macAddress")) {
                            Log.e("TelemetryScreen", "macAddress is null")
                            return@composable
                        }

                        val macAddress = it.arguments!!.getString("macAddress")!!

                        telemetryViewModel.onEvent(
                            TelemetryEvent.SetMacAddress(macAddress)
                        )

                        LaunchedEffect(key1 = macAddress) {
                            telemetryViewModel.onEvent(
                                TelemetryEvent.ClearServiceCache
                            )
                        }

                        TelemetryScreen(
                            state = telemetryState,
                            onEvent = telemetryViewModel::onEvent
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