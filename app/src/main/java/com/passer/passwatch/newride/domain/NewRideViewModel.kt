package com.passer.passwatch.newride.domain

import android.Manifest
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.BluetoothGattContainer
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.repo.data.RouteDao
import com.passer.passwatch.core.util.convertToBytes
import com.passer.passwatch.nearpass.data.NearPassDao
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
    private val locationManager: LocationManager,
    private val routeDao: RouteDao,
    private val nearPassDao: NearPassDao,
) : ViewModel() {
    private val _state = MutableStateFlow(NewRideState())
    private val _nearpasses = _state.flatMapLatest {
        nearPassDao.getNearPassesForRide(it.rideId)
    }
    private val _routes = _state.flatMapLatest {
        routeDao.getRoutesForRide(it.rideId)
    }
    private val _permissionNeeded = MutableSharedFlow<String>()

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun onEvent(event: NewRideEvent) {
        when (event) {
            is NewRideEvent.StartRide -> {
                if (!BluetoothGattContainer.isConnected()) {
                    _state.update {
                        it.copy(
                            rideStatusMessage = "Connect to an NPITS device first!"
                        )
                    }
                    return
                }
                _state.update {
                    it.copy(
                        rideStatusMessage = "Ride Started!"
                    )
                }

                BluetoothGattContainer.emplace(
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_TIME.uuid,
                    convertToBytes(System.currentTimeMillis().div(1000))
                )

                BluetoothGattContainer.emplace(UUIDConstants.SERVICE_RIDE_CONTROL.uuid, UUIDConstants.RC_CMD.uuid, convertToBytes(1))
                BluetoothGattContainer.flush()

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
                    LocationManager.NETWORK_PROVIDER,
                    10000,
                    0.0001f,
                    locationListener
                )

                viewModelScope.launch {

                    // Initialize values
                    _state.update {
                        it.copy(
                            rideStarted = true,
                            rideStartTime = System.currentTimeMillis(),
                            rideTime = 0
                        )
                    }
                }
            }

            is NewRideEvent.StopRide -> {
                timerJob?.cancel()
                locationManager.removeUpdates(locationListener)

                BluetoothGattContainer.emplace(UUIDConstants.SERVICE_RIDE_CONTROL.uuid, UUIDConstants.RC_CMD.uuid, convertToBytes(0))
                BluetoothGattContainer.flush()

                _state.update {
                    it.copy(
                        rideStarted = false,
                        rideStatusMessage = "Ride Ended"
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

        if (BluetoothGattContainer.isConnected()) {
            Log.i("NewRideViewModel", "Sending location to device: $location")

            viewModelScope.launch {
                BluetoothGattContainer.clear()

                BluetoothGattContainer.emplace(
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_LATITUDE.uuid,
                    convertToBytes(location.latitude)
                )

                BluetoothGattContainer.emplace(
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_LONGITUDE.uuid,
                    convertToBytes(location.longitude)
                )

                BluetoothGattContainer.emplace(
                    UUIDConstants.SERVICE_GPS_COORDS.uuid,
                    UUIDConstants.GPS_SPEED_MPS.uuid,
                    convertToBytes(location.speed.toInt())
                )

                BluetoothGattContainer.flush()
            }
        }else{
            Log.i("NewRideViewModel", "No GATT connection, cannot update location")
        }
    }
}