package com.passer.passwatch.settings.domain

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.ble.readFromBluetoothGattCharacteristic
import com.passer.passwatch.core.ble.writeToBluetoothGattCharacteristic
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.core.util.convertFromBytes
import com.passer.passwatch.nearpass.data.NearPass
import com.passer.passwatch.nearpass.data.NearPassDao
import com.passer.passwatch.ride.data.Ride
import com.passer.passwatch.ride.data.RideDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val applicationContext: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bluetoothManager: BluetoothManager,
    private val nearPassDao: NearPassDao,
    private val rideDao: RideDao,
) : ViewModel() {
    private val scanPeriod: Long = 10000
    private val bleScanner = bluetoothManager.adapter.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(SettingsState())
    private val _hubMacAddress = userPreferencesRepository.hubMacAddress
    private val _permissionNeeded = MutableSharedFlow<String>()

    val state = combine(_state, _hubMacAddress) { state, hubMacAddress ->
        state.copy(
            hubMacAddress = hubMacAddress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())
    val permissionNeeded = _permissionNeeded.asSharedFlow()

    private val hubMacAddress = _hubMacAddress.stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    @SuppressLint("MissingPermission")
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SaveMacAddress -> {
                val newMacAddress = event.newMacAddress

                viewModelScope.launch {
                    userPreferencesRepository.saveHubMacAddress(newMacAddress)
                }

                _state.update {
                    it.copy(newHubMacAddress = "")
                }
            }

            is SettingsEvent.SetMacAddress -> {
                _state.update {
                    it.copy(newHubMacAddress = event.newMacAddress)
                }
            }

            is SettingsEvent.StartScan -> {
                viewModelScope.launch {
                    _permissionNeeded.emit(Manifest.permission.BLUETOOTH_CONNECT)
                    val scanSettings: ScanSettings = ScanSettings.Builder()
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build()


                    handler.postDelayed({
                        onEvent(SettingsEvent.StopScan)
                    }, scanPeriod)

                    _state.update {
                        it.copy(
                            scannedDevices = emptyList(),
                            scanning = true
                        )
                    }

                    bleScanner.startScan(null, scanSettings, scanCallback)
                }
            }

            is SettingsEvent.StopScan -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            scanning = false
                        )
                    }

                    bleScanner.stopScan(scanCallback)
                }
            }

            is SettingsEvent.RequestPermissions -> {
                viewModelScope.launch {
                    _permissionNeeded.emit(Manifest.permission.BLUETOOTH_SCAN)
                }
            }

            is SettingsEvent.SyncData -> {
                var bluetoothGatt: BluetoothGatt? = null

                _state.update {
                    it.copy(
                        syncStatus = "Starting Sync"
                    )
                }

                Log.i("SettingsViewModel", "Connecting to device: ${hubMacAddress.value}")
                bluetoothManager.adapter?.let { adapter ->
                    try {
                        val device = adapter.getRemoteDevice(hubMacAddress.value)
                        bluetoothGatt =
                            device.connectGatt(applicationContext, true, bluetoothGattCallback)

                    } catch (exception: IllegalArgumentException) {
                        Log.w(
                            "SettingsViewModel",
                            "Device not found with MAC address: ${hubMacAddress.value}"
                        )
                    }
                } ?: run {
                    Log.w("SettingsViewModel", "Bluetooth adapter is null")
                }

                _state.update {
                    it.copy(
                        syncStatus = "Syncing Near Passes"
                    )
                }

                viewModelScope.launch {
                    nearPassDao.deleteAllNearPasses()
                }
                writeToBluetoothGattCharacteristic(bluetoothGatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NPL_REQUEST.uuid, 1)


                _state.update {
                    it.copy(
                        syncStatus = "Syncing Rides"
                    )
                }

                viewModelScope.launch {
                    rideDao.deleteAllRides()
                }
                writeToBluetoothGattCharacteristic(bluetoothGatt, UUIDConstants.SERVICE_RIDES_LIST.uuid, UUIDConstants.RL_REQUEST.uuid, 1)


                _state.update {
                    it.copy(
                        syncStatus = "Sync Completed!"
                    )
                }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!state.value.scannedDevices.contains(device)) {
                _state.update {
                    it.copy(scannedDevices = _state.value.scannedDevices + device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.i("BleScannerViewModel", "onScanFailed: $errorCode")
            super.onScanFailed(errorCode)
        }
    }

    @SuppressLint("MissingPermission")
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        private var localNearPass = NearPass(0.0,0.0,0.0,0.0,0,0)
        private var localRide = Ride(0,0,0)
        private var NP_counter = 0
        private var R_counter = 0

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("SettingsViewModel", "Connection state changed to $newState")

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i("SettingsViewModel", "Connected to device")
                gatt?.discoverServices()
                gatt?.requestMtu(200)
            } else {
                Log.i("SettingsViewModel", "Disconnected from device")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("SettingsViewModel", "Services discovered")

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

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)

            if(characteristic.uuid == UUIDConstants.NPL_ID.uuid){
                localNearPass.id = convertFromBytes<Int>(value)!!
                NP_counter++
            }
            if(characteristic.uuid == UUIDConstants.NP_UNIX_TIME.uuid){
                localNearPass.time = convertFromBytes(value)
                NP_counter++
            }
            if(characteristic.uuid == UUIDConstants.NP_LATITUDE.uuid){
                localNearPass.latitude = convertFromBytes(value)
                NP_counter++
            }
            if(characteristic.uuid == UUIDConstants.NP_LONGITUDE.uuid){
                localNearPass.longitude = convertFromBytes(value)
                NP_counter++
            }
            if(characteristic.uuid == UUIDConstants.NP_SPEED_MPS.uuid){
                localNearPass.speed = convertFromBytes(value)
                NP_counter++
            }
            if(characteristic.uuid == UUIDConstants.NP_DISTANCE_CM.uuid){
                localNearPass.distance = convertFromBytes(value)
                NP_counter++
            }

            if(NP_counter == 6){
                NP_counter = 0
                viewModelScope.launch {
                    nearPassDao.insertNearPass(localNearPass)
                }
            }

            if(characteristic.uuid == UUIDConstants.R_ID.uuid){
                localRide.id = convertFromBytes<Int>(value)!!
                R_counter++;
            }
            if(characteristic.uuid == UUIDConstants.R_START_UNIX_TIME.uuid){
                localRide.startTime = convertFromBytes(value)
                R_counter++;
            }
            if(characteristic.uuid == UUIDConstants.R_END_UNIX_TIME.uuid){
                localRide.endTime = convertFromBytes(value)
                R_counter++;
            }

            if(R_counter == 3){
                R_counter = 0;
                viewModelScope.launch {
                    rideDao.insertRide(localRide)
                }
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            if(characteristic.uuid == UUIDConstants.NP_VALID.uuid){
                readFromBluetoothGattCharacteristic<Int>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NPL_ID.uuid)
                readFromBluetoothGattCharacteristic<Long>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_UNIX_TIME.uuid)
                readFromBluetoothGattCharacteristic<Double>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_LATITUDE.uuid)
                readFromBluetoothGattCharacteristic<Double>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_LONGITUDE.uuid)
                readFromBluetoothGattCharacteristic<Double>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_SPEED_MPS.uuid)
                readFromBluetoothGattCharacteristic<Double>(gatt, UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_DISTANCE_CM.uuid)
            }

            if(characteristic.uuid == UUIDConstants.SERVICE_RIDES_LIST.uuid){
                readFromBluetoothGattCharacteristic<Int>(gatt, UUIDConstants.SERVICE_RIDES_LIST.uuid, UUIDConstants.R_ID.uuid)
                readFromBluetoothGattCharacteristic<Long>(gatt, UUIDConstants.SERVICE_RIDES_LIST.uuid, UUIDConstants.R_START_UNIX_TIME.uuid)
                readFromBluetoothGattCharacteristic<Long>(gatt, UUIDConstants.SERVICE_RIDES_LIST.uuid, UUIDConstants.R_END_UNIX_TIME.uuid)
            }
        }

    }
}


