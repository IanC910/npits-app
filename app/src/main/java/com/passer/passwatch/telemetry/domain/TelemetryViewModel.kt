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
        var characteristics: List<BluetoothGattCharacteristic> = emptyList()

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

            characteristics = emptyList()

            for (service in gatt!!.services) {
                Log.i("TelemetryViewModel", "Service: ${service.uuid}")
                for (characteristic in service.characteristics) {
                    Log.i("TelemetryViewModel", "Characteristic: ${characteristic.uuid}")

                    if (characteristic.properties.and(BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        characteristics += characteristic
                    }
                }
            }

            subscribeToCharacteristics(gatt)
        }

        private fun subscribeToCharacteristics(gatt: BluetoothGatt) {
            if (characteristics.isEmpty()) {
                Log.i("TelemetryViewModel", "reading all characteristics")
                for (service in gatt.services) {
                    for (characteristic in service.characteristics) {
                        Log.i("TelemetryViewModel", "Characteristic: ${characteristic.uuid}")

                        if (characteristic.properties.and(BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                            Log.i(
                                "TelemetryViewModel",
                                "Characteristic ${characteristic.uuid} read"
                            )
                            val ret = gatt.readCharacteristic(characteristic)
                            Log.i("Telemetry ViewModel", "read ret is $ret for ${characteristic.uuid}")
                        }

                    }
                }
                return
            }

            val characteristic: BluetoothGattCharacteristic = characteristics.last()

            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                Log.e(
                    "TelemetryViewModel",
                    "Characteristic ${characteristic.uuid} notification set failed"
                )
            } else {
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

                Log.i(
                    "TelemetryViewModel",
                    "Characteristic ${characteristic.uuid} notification set"
                )

                val descriptor = characteristic.getDescriptor(characteristic.descriptors[0].uuid)

                if (descriptor != null) {
                    val ret = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    Log.i("TelemetryViewModel", "Write descriptor returned $ret, ${characteristics.size} elements left to process")

                } else {
                    Log.e(
                        "TelemetryViewModel",
                        "CCCD descriptor not found for characteristic ${characteristic.uuid}"
                    )
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            characteristics = characteristics.dropLast(1)
            subscribeToCharacteristics(gatt!!)
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
                            + (characteristic.uuid.toString() to value.contentToString())
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
                            + (characteristic.uuid.toString() to value.contentToString())
                )
            }
        }
    }
}
