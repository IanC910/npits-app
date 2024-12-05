package com.passer.passwatch

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
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
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.passer.passwatch.map.domain.MapEvent
import com.passer.passwatch.map.domain.MapViewModel
import com.passer.passwatch.map.presentation.MapScreen
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassViewModel
import com.passer.passwatch.nearpass.presentation.NearPassScreen
import com.passer.passwatch.newride.domain.NewRideEvent
import com.passer.passwatch.newride.domain.NewRideViewModel
import com.passer.passwatch.newride.presentation.NewRideScreen
import com.passer.passwatch.ride.domain.RideEvent
import com.passer.passwatch.ride.domain.RideViewModel
import com.passer.passwatch.ride.presentation.RidesScreen
import com.passer.passwatch.settings.domain.SettingsEvent
import com.passer.passwatch.settings.domain.SettingsViewModel
import com.passer.passwatch.settings.presentation.SettingsScreen
import com.passer.passwatch.telemetry.domain.TelemetryEvent
import com.passer.passwatch.telemetry.domain.TelemetryViewModel
import com.passer.passwatch.telemetry.presentation.TelemetryScreen
import com.passer.passwatch.ui.theme.PassWatchTheme
import kotlinx.coroutines.launch

private const val PREFERENCE_NAME = "user_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCE_NAME
)

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
                    val locationManager =
                        getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    return NewRideViewModel(
                        locationManager,
                        db.routeDao,
                        db.nearPassDao
                    ) as T
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
                    return RideViewModel(applicationContext, db.rideDao, db.nearPassDao) as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val mapViewModel by viewModels<MapViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MapViewModel(db.routeDao, db.nearPassDao) as T
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
                    return SettingsViewModel(applicationContext, userRepo, bluetoothManager, db.nearPassDao, db.rideDao) as T
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

        setContent {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    newRideViewModel.permissionNeeded.collect { permission ->
                        requestPermission(permission)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    settingsViewModel.permissionNeeded.collect { permission ->
                        requestPermission(permission)
                    }
                }
            }

            PassWatchTheme {
                val newRideState by newRideViewModel.state.collectAsState()
                val nearPassState by nearPassViewModel.state.collectAsState()
                val rideState by rideViewModel.state.collectAsState()
                val mapState by mapViewModel.state.collectAsState()
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
                        LaunchedEffect(key1 = "") {
                            newRideViewModel.onEvent(NewRideEvent.RequestPermissions)
                        }

                        NewRideScreen(newRideState, newRideViewModel::onEvent)
                    }

                    composable<RidesScreen> {
                        LaunchedEffect(key1 = "") {
                            Log.i("MainActivity", "Requesting permissions")
                            rideViewModel.onEvent(RideEvent.RequestPermissions)
                        }

                        RidesScreen(
                            navController = navController,
                            state = rideState,
                            onEvent = rideViewModel::onEvent
                        )
                    }

                    composable<MapScreen> {
                        if (it.arguments == null) {
                            Log.e("MapScreen", "arguments is null")
                            return@composable
                        }

                        if (!it.arguments!!.containsKey("rideId")) {
                            Log.e("MapScreen", "rideId is null")
                            return@composable
                        }

                        mapViewModel.onEvent(
                            MapEvent.SetRideId(it.arguments!!.getInt("rideId"))
                        )

                        MapScreen(
                            state = mapState
                        )
                    }

                    composable<SettingsScreen> {
                        LaunchedEffect(key1 = "") {
                            Log.i("MainActivity", "Requesting permissions")
                            settingsViewModel.onEvent(SettingsEvent.RequestPermissions)
                        }

                        SettingsScreen(
                            state = settingsState,
                            onEvent = settingsViewModel::onEvent
                        )

                        LaunchedEffect(key1 = "") {
                            Log.i("MainActivity", "Load GoPro creds")
                            settingsViewModel.onEvent(SettingsEvent.LoadGoProCredentials)
                        }
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

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity", "Requesting $permission permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }

}