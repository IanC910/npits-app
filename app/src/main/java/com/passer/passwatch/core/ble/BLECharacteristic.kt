package com.passer.passwatch.core.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.passer.passwatch.core.util.convertFromBytes
import com.passer.passwatch.core.util.convertToBytes
import java.util.UUID

fun <T> writeToBluetoothGattCharacteristic(
    bluetoothGatt: BluetoothGatt?,
    serviceUUID: UUID,
    characteristicUUID: UUID,
    value: T
): Int {
    // Ensure that bluetoothGatt is not null
    bluetoothGatt?.let {
        // Get the service using the service UUID
        val service = bluetoothGatt.getService(serviceUUID)

        if (service != null) {
            // Get the characteristic using the characteristic UUID
            val characteristic = service.getCharacteristic(characteristicUUID)

            if (characteristic != null) {
                // Convert the value to a byte array
                val byteArray = convertToBytes(value)

                // Set the byte array to the characteristic
                characteristic.value = byteArray

                // Write the characteristic
                val result = bluetoothGatt.writeCharacteristic(characteristic, byteArray, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                println("Write characteristic result: $result")
                if(result != 0) {
                    return result
                }
            } else {
                println("Characteristic with UUID $characteristicUUID not found")
                return 1
            }
        } else {
            println("Service with UUID $serviceUUID not found")
            return 1
        }
    }

    return 0
}

inline fun <reified T> readFromBluetoothGattCharacteristic(
    bluetoothGatt: BluetoothGatt?,
    serviceUUID: UUID,
    characteristicUUID: UUID
): T? {
    // Ensure that bluetoothGatt is not null
    bluetoothGatt?.let {
        // Get the service using the service UUID
        val service = bluetoothGatt.getService(serviceUUID)

        if (service != null) {
            // Get the characteristic using the characteristic UUID
            val characteristic = service.getCharacteristic(characteristicUUID)

            if (characteristic != null) {
                // Attempt to read the characteristic
                bluetoothGatt.readCharacteristic(characteristic)
            } else {
                Log.i("BLECharacteristic", "Characteristic with UUID $characteristicUUID not found")
            }
        } else {
            Log.i("BLECharacteristic", "Service with UUID $serviceUUID not found")
        }
    } ?: Log.i("BLECharacteristic", "BluetoothGatt is null")

    return null // Return null if any step fails
}


