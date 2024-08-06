package com.passer.passwatch.telemetry.domain


import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TelemetryViewModel(
    private val applicationContext: Context,
    private val bluetoothManager: BluetoothManager,
) : ViewModel() {
    private lateinit var computationalUnit: BluetoothDevice
    private var bluetoothGatt: BluetoothGatt? = null

    private val _state = MutableStateFlow(TelemetryState())

    val state = _state.asStateFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), TelemetryState()
    )


    @SuppressLint("MissingPermission")
    fun onEvent(event: TelemetryEvent) {
        when (event) {
            is TelemetryEvent.SetMacAddress -> {
                _state.update {
                    it.copy(
                        macAddress = event.newMacAddress
                    )
                }
            }

            is TelemetryEvent.ConnectDevice -> {
                bluetoothGatt?.close()

                Log.i("TelemetryViewModel", "Connecting to device")
                bluetoothManager.adapter?.let { adapter ->
                    try {
                        val device = adapter.getRemoteDevice(state.value.macAddress)
                        bluetoothGatt =
                            device.connectGatt(applicationContext, true, bluetoothGattCallback)

                    } catch (exception: IllegalArgumentException) {
                        Log.w("TelemetryViewModel", "Device not found with provided MAC address")
                    }
                } ?: run {
                    Log.w("TelemetryViewModel", "Bluetooth adapter is null")
                }
            }

            is TelemetryEvent.ClearServiceCache -> {
                _state.update {
                    it.copy(
                        services = emptyList()
                    )
                }
            }
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

            _state.update {
                it.copy(
                    services = gatt!!.services
                )
            }

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

                            val descriptor = characteristic.getDescriptor(characteristic.descriptors[0].uuid)

                            if (descriptor != null) {
                                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            } else {
                                Log.e(
                                    "TelemetryViewModel",
                                    "CCCD descriptor not found for characteristic ${characteristic.uuid}"
                                )
                            }
                        }
                    }

                    if (characteristic.properties.and(BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        gatt.readCharacteristic(characteristic)
                    }

                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.i(
                "TelemetryViewModel",
                "Characteristic ${characteristic.uuid} changed to ${String(value)}"
            )
            _state.update {
                it.copy(
                    characteristicValue = it.characteristicValue
                            + (characteristic.uuid.toString() to String(value))
                )
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.i(
                "TelemetryViewModel",
                "Characteristic ${characteristic.uuid} read with value ${String(value)}"
            )
            Log.i(
                "TelemetryViewModel",
                "Characteristic ${characteristic.uuid} read with value ${value.contentToString()}"
            )

            _state.update {
                it.copy(
                    characteristicValue = it.characteristicValue
                            + (characteristic.uuid.toString() to String(value))
                )
            }
        }
    }
}
