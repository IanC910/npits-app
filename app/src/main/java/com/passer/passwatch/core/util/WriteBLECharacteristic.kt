package com.passer.passwatch.core.util

import android.bluetooth.BluetoothGatt
import java.util.UUID

fun <T> writeToBluetoothGattCharacteristic(
    bluetoothGatt: BluetoothGatt?,
    serviceUUID: UUID,
    characteristicUUID: UUID,
    value: T
) {
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
                val success = bluetoothGatt.writeCharacteristic(characteristic)

                if (success) {
                    println("Successfully wrote characteristic with UUID: $characteristicUUID")
                } else {
                    println("Failed to write characteristic with UUID: $characteristicUUID")
                }
            } else {
                println("Characteristic with UUID $characteristicUUID not found")
            }
        } else {
            println("Service with UUID $serviceUUID not found")
        }
    }
}