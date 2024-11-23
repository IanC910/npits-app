package com.passer.passwatch.newride.domain

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.core.repo.data.Route
import com.passer.passwatch.core.repo.data.RouteDao
import com.passer.passwatch.core.util.convertToBytes
import com.passer.passwatch.core.util.writeToBluetoothGattCharacteristic
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

                // Connect to bluetooth
                bluetoothGatt?.close()

                Log.i("TelemetryViewModel", "Connecting to device: ${hubMacAddress.value}")
                bluetoothManager.adapter?.let { adapter ->
                    try {
                        val device = adapter.getRemoteDevice(hubMacAddress.value)
                        bluetoothGatt =
                            device.connectGatt(applicationContext, true, bluetoothGattCallback)

                    } catch (exception: IllegalArgumentException) {
                        Log.w(
                            "TelemetryViewModel",
                            "Device not found with MAC address: ${hubMacAddress.value}"
                        )
                    }
                } ?: run {
                    Log.w("TelemetryViewModel", "Bluetooth adapter is null")
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


        viewModelScope.launch {
            writeToBluetoothGattCharacteristic(
                bluetoothGatt,
                UUIDConstants.SERVICE_GPS_COORDS.uuid,
                UUIDConstants.GPS_LATITUDE.uuid,
                convertToBytes(location.latitude)
            )
            writeToBluetoothGattCharacteristic(
                bluetoothGatt,
                UUIDConstants.SERVICE_GPS_COORDS.uuid,
                UUIDConstants.GPS_LONGITUDE.uuid,
                convertToBytes(location.longitude)
            )
            writeToBluetoothGattCharacteristic(
                bluetoothGatt,
                UUIDConstants.SERVICE_GPS_COORDS.uuid,
                UUIDConstants.GPS_SPEED_MPS.uuid,
                convertToBytes(location.speed)
            )
        }
    }


    @SuppressLint("MissingPermission")
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("TelemetryViewModel", "Connection state changed to $newState")

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i("TelemetryViewModel", "Connected to device")
                gatt?.discoverServices()
                gatt?.requestMtu(200)
            } else {
                Log.i("TelemetryViewModel", "Disconnected from device")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("TelemetryViewModel", "Services discovered")

            for (service in gatt!!.services) {
                Log.i("TelemetryViewModel", "Service: ${service.uuid}")
                for (characteristic in service.characteristics) {
                    Log.i("TelemetryViewModel", "Characteristic: ${characteristic.uuid}")

                    if (characteristic.properties.and(BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        if (!gatt.setCharacteristicNotification(characteristic, true)) {
                            Log.e(
                                "TelemetryViewModel",
                                "Characteristic ${characteristic.uuid} notification set failed"
                            )
                        } else {
                            Log.i(
                                "TelemetryViewModel",
                                "Characteristic ${characteristic.uuid} notification set"
                            )

                            val descriptor =
                                characteristic.getDescriptor(characteristic.descriptors[0].uuid)

                            if (descriptor != null) {
                                gatt.writeDescriptor(
                                    descriptor,
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                )
                            } else {
                                Log.e(
                                    "TelemetryViewModel",
                                    "CCCD descriptor not found for characteristic ${characteristic.uuid}"
                                )
                            }
                        }
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
//            Log.i(
//                "TelemetryViewModel",
//                "Characteristic ${characteristic.uuid} changed to ${String(value)}"
//            )

            _state.update {
                it.copy(
                    characteristicValue = it.characteristicValue
                            + (characteristic.uuid.toString() to String(value))
                )
            }
        }
    }
}