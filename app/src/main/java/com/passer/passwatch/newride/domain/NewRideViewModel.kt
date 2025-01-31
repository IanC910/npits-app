package com.passer.passwatch.newride.domain

import android.Manifest
import android.location.LocationListener
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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
    private val fusedLocationProviderClient: FusedLocationProviderClient,
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


                val locationRequest = LocationRequest.create().apply {
                    interval = 5000 // 10 seconds interval
                    fastestInterval = 2000 // 5 seconds fastest interval
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

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
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)

                if(!BluetoothGattContainer.isConnected()){
                    _state.update {
                        it.copy(
                            rideStatusMessage = "Connect to an NPITS device before stopping the ride!"
                        )
                    }
                    return
                }

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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            if(locationResult.locations.isEmpty()){
                return
            }

            val location = locationResult.locations.last()
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
}