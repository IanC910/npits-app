package com.passer.passwatch.settings.domain

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.BluetoothGattContainer
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.repo.UserPreferencesRepository
import com.passer.passwatch.core.util.MPS_TO_KMPH
import com.passer.passwatch.core.util.convertFromBytes
import com.passer.passwatch.core.util.convertToBytes
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

    private var bluetoothGatt: BluetoothGatt? = null

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

            is SettingsEvent.Connect -> {
                if(hubMacAddress.value == "") {
                    _state.update {
                        it.copy(connectionState = "Select a device first!")
                    }
                    return
                }

                Log.i("SettingsViewModel", "Connecting to device: ${hubMacAddress.value}")
                bluetoothManager.adapter?.let { adapter ->
                    try {
                        viewModelScope.launch {
                            if(BluetoothGattContainer.isConnected()){
                                _state.update {
                                    it.copy(connectionState = "Resetting Connection...")
                                }

                                BluetoothGattContainer.disconnect()
                                delay(2000)
                            }

                            _state.update {
                                it.copy(connectionState = "Connecting...")
                            }

                            val device = adapter.getRemoteDevice(hubMacAddress.value)
                            BluetoothGattContainer.gatt = device.connectGatt(applicationContext, true, bluetoothGattCallback)
                        }

                    } catch (exception: IllegalArgumentException) {
                        Log.w(
                            "SettingsViewModel",
                            "Device not found with MAC address: ${hubMacAddress.value}"
                        )
                        _state.update {
                            it.copy(connectionState = "No device with specified MAC address!")
                        }
                    }
                } ?: run {
                    Log.w("SettingsViewModel", "Bluetooth adapter is null")
                }
            }

            is SettingsEvent.SyncData -> {
//                if(!BluetoothGattContainer.isConnected()) {
//                    _state.update {
//                        it.copy(syncStatus = "Connect to a device first!")
//                    }
//                    return
//                }
//
//                _state.update {
//                    it.copy(syncStatus = "Syncing Rides")
//                }
//
//                viewModelScope.launch {
//                    withContext(Dispatchers.IO) {
//                        rideDao.deleteAllRides() // Suspend until deletion is complete
//                    }
//
//                    BluetoothGattContainer.emplace(UUIDConstants.SERVICE_RIDES_LIST.uuid,
//                        UUIDConstants.RL_REQUEST.uuid,
//                        convertToBytes(1)
//                    )
//
//                    BluetoothGattContainer.flush()
//                }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!state.value.scannedDevices.contains(device) && device.name == "NPITS") {
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

        private var localNearPass = NearPass(0.0,0.0,0.0,0.0,0)
        private var localRide = Ride(0,0)

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("SettingsViewModel", "Connection state changed to $newState")

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i("SettingsViewModel", "Connected to device")
                gatt?.discoverServices()
                gatt?.requestMtu(200)

            } else {
                Log.i("SettingsViewModel", "Disconnected from device")
                _state.update {
                    it.copy(connectionState = "Disconnected")
                }
                BluetoothGattContainer.disconnect()
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("SettingsViewModel", "Services discovered")

            BluetoothGattContainer.clearDescriptors()

            for (service in gatt!!.services) {
                Log.i("SettingsViewModel", "Service: ${service.uuid}")
                for (characteristic in service.characteristics) {
                    Log.i("SettingsViewModel", "Characteristic: ${characteristic.uuid}")

                    if (characteristic.properties.and(BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        BluetoothGattContainer.emplaceDescriptor(characteristic)
                    }
                }
            }

            BluetoothGattContainer.flushDescriptors()
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            BluetoothGattContainer.removeLastDescriptor()

            if(BluetoothGattContainer.isDescriptorQueueEmpty()){
                Log.i("SettingsViewModel", "Descriptor queue is empty")
                _state.update {
                    it.copy(connectionState = "Connected!")
                }
            }else{
                BluetoothGattContainer.flushDescriptors()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            Log.i("SettingsViewModel", "Characteristic " + characteristic.uuid.toString() + " changed: " + value.contentToString())
            Log.i("TEST", "${convertFromBytes<Int>(value) == 1}")


            if(characteristic.uuid == UUIDConstants.NP_VALID.uuid && convertFromBytes<Int>(value) == 1) {
                Log.i("SettingsViewModel", "Logging Near Pass to DB")

                viewModelScope.launch {
                    nearPassDao.insertNearPass(localNearPass)
                }
                BluetoothGattContainer.emplace(UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid, UUIDConstants.NP_VALID.uuid, convertToBytes(0))
                BluetoothGattContainer.flush()

                localNearPass = NearPass(0.0,0.0,0.0,0.0,0)
            }

            if(characteristic.uuid == UUIDConstants.R_VALID.uuid && convertFromBytes<Int>(value) == 1) {
                Log.i("SettingsViewModel", "Logging Ride to DB")

                viewModelScope.launch {
                    rideDao.insertRide(localRide)
                }

                BluetoothGattContainer.emplace(UUIDConstants.SERVICE_RIDES_LIST.uuid, UUIDConstants.R_VALID.uuid, convertToBytes(0))
                BluetoothGattContainer.flush()

                localRide = Ride(0,0)
            }

            if(characteristic.uuid == UUIDConstants.RL_REQUEST.uuid && convertFromBytes<Int>(value) == 0) {
                viewModelScope.launch {
                    Thread.sleep(1000)

//                    _state.update {
//                        it.copy(syncStatus = "Syncing Near Passes")
//                    }
                    Toast.makeText(applicationContext, "Syncing Near Passes", Toast.LENGTH_SHORT).show()

                    withContext(Dispatchers.IO) {
                        nearPassDao.deleteAllNearPasses()
                    }

                    BluetoothGattContainer.emplace(UUIDConstants.SERVICE_NEAR_PASS_LIST.uuid,
                        UUIDConstants.NPL_REQUEST.uuid,
                        convertToBytes(1)
                    )

                    BluetoothGattContainer.flush()
                }
            }

            if(characteristic.uuid == UUIDConstants.NPL_REQUEST.uuid && convertFromBytes<Int>(value) == 0) {
//                _state.update {
//                    it.copy(syncStatus = "Sync Done!")
//                }
                viewModelScope.launch {
                    Toast.makeText(applicationContext, "Sync Done!", Toast.LENGTH_SHORT).show()
                }
            }

            when (characteristic.uuid) {
                UUIDConstants.NP_RIDE_ID.uuid -> {
                    localNearPass.rideId = convertFromBytes<Int>(value)!!
                }
                UUIDConstants.NP_TIME.uuid -> {
                    localNearPass.time = convertFromBytes<Long>(value)?.times(1000)
                }
                UUIDConstants.NP_LATITUDE.uuid -> {
                    localNearPass.latitude = convertFromBytes<Double>(value)
                }
                UUIDConstants.NP_LONGITUDE.uuid -> {
                    localNearPass.longitude = convertFromBytes<Double>(value)
                }
                UUIDConstants.NP_SPEED_MPS.uuid -> {
                    localNearPass.speed = convertFromBytes<Double>(value)?.times(MPS_TO_KMPH)
                }
                UUIDConstants.NP_DISTANCE_CM.uuid -> {
                    localNearPass.distance = convertFromBytes<Int>(value)?.toDouble()
                }


                UUIDConstants.R_ID.uuid -> {
                    localRide.id = convertFromBytes<Int>(value)!!
                }
                UUIDConstants.R_START_TIME.uuid -> {
                    localRide.startTime = convertFromBytes<Long>(value)?.times(1000)
                }
                UUIDConstants.R_END_TIME.uuid -> {
                    localRide.endTime = convertFromBytes<Long>(value)?.times(1000)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i("SettingsViewModel", "Characteristic ${characteristic?.uuid} written")
            BluetoothGattContainer.removeLast()
            BluetoothGattContainer.flush()
        }
    }
}


