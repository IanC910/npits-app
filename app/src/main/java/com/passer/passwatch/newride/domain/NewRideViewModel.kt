package com.passer.passwatch.newride.domain

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.BluetoothGattContainer
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.core.repo.data.Route
import com.passer.passwatch.core.repo.data.RouteDao
import com.passer.passwatch.core.util.convertToBytes
import com.passer.passwatch.core.ble.writeToBluetoothGattCharacteristic
import com.passer.passwatch.nearpass.data.NearPassDao
import com.passer.passwatch.ride.data.Ride
import com.passer.passwatch.ride.data.RideDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewRideViewModel(
    private val applicationContext: Context,
    userPreferencesRepository: UserPreferencesRepository,
    private val locationManager: LocationManager,
    private val bluetoothManager: BluetoothManager,
    private val rideDao: RideDao,
    private val routeDao: RouteDao,
    private val nearPassDao: NearPassDao,
) : ViewModel() {
    private val _state = MutableStateFlow(NewRideState())
    private val _hubMacAddress = userPreferencesRepository.hubMacAddress
    private val _nearpasses = _state.flatMapLatest {
        nearPassDao.getNearPassesForRide(it.rideId)
    }
    private val _routes = _state.flatMapLatest {
        routeDao.getRoutesForRide(it.rideId)
    }
    private val _permissionNeeded = MutableSharedFlow<String>()

    private val hubMacAddress = _hubMacAddress.stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val state = combine(_state, _nearpasses, _routes) { state, nearpasses, routes ->
        state.copy(
            nearPasses = nearpasses,
            routes = routes
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), NewRideState()
    )

    val permissionNeeded = _permissionNeeded.asSharedFlow()


    private var timerJob: Job? = null
    private var bluetoothGatt: BluetoothGatt? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun onEvent(event: NewRideEvent) {
        when (event) {
            is NewRideEvent.StartRide -> {
                if(!BluetoothGattContainer.isConnected()) {
                    _state.update {
                        it.copy(
                            rideStatus = false,
                            rideStatusMessage = "Connect to an NPITS device first!"
                        )
                    }
                    return
                }
                _state.update {
                    it.copy(
                        rideStatus = true,
                        rideStatusMessage = "Ride Started!"
                    )
                }

                // Start the timer
                timerJob?.cancel()
                timerJob = viewModelScope.launch {
                    while (true) {
                        delay(1000)
                        _state.update {
                            it.copy(
                                rideTime = it.rideTime.inc()
                            )
                        }
                    }
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0.0001f,
                    locationListener
                )

                // Create a new Ride
                val ride = Ride(
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                )

                viewModelScope.launch {
                    val newRideId =
                        rideDao.insertRide(ride)


                    // Initialize values
                    _state.update {
                        it.copy(
                            rideStarted = true,
                            rideStartTime = System.currentTimeMillis(),
                            rideTime = 0,
                            rideId = newRideId.toInt()
                        )
                    }
                }
            }

            is NewRideEvent.StopRide -> {
                timerJob?.cancel()
                bluetoothGatt?.close()
                locationManager.removeUpdates(locationListener)

                viewModelScope.launch {
                    rideDao.updateRideEndTime(state.value.rideId, System.currentTimeMillis())
                }

                _state.update {
                    it.copy(
                        rideStarted = false
                    )
                }
            }

            is NewRideEvent.RequestPermissions -> {
                viewModelScope.launch {
                    _permissionNeeded.emit(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        onEvent(NewRideEvent.StopRide)

        Log.i("NewRideViewModel", "onCleared")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val locationListener = LocationListener { location ->
        Log.i("NewRideViewModel", "Location changed: $location")

        val route = Route(
            latitude = location.latitude,
            longitude = location.longitude,
            speed = location.speed.toDouble(),
            time = System.currentTimeMillis(),
            rideId = state.value.rideId
        )

        if(BluetoothGattContainer.isConnected()) {
            viewModelScope.launch {
                writeToBluetoothGattCharacteristic(
                    BluetoothGattContainer.gatt,
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_LATITUDE.uuid,
                    location.latitude
                )

                Thread.sleep(1000)

                writeToBluetoothGattCharacteristic(
                    BluetoothGattContainer.gatt,
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_LONGITUDE.uuid,
                    location.longitude
                )

                Thread.sleep(1000)

                writeToBluetoothGattCharacteristic(
                    BluetoothGattContainer.gatt,
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_SPEED_MPS.uuid,
                    location.speed.toInt()
                )

                Thread.sleep(1000)

            }
        }
    }
}