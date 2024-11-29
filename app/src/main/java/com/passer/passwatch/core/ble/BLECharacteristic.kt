package com.passer.passwatch.core.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

fun writeToBluetoothGattCharacteristic(
    bluetoothGatt: BluetoothGatt?,
    serviceUUID: UUID,
    characteristicUUID: UUID,
    byteArray: ByteArray
): Int {
    // Ensure that bluetoothGatt is not null
    bluetoothGatt?.let {
        // Get the service using the service UUID
        val service = bluetoothGatt.getService(serviceUUID)

        if (service != null) {
            // Get the characteristic using the characteristic UUID
            val characteristic = service.getCharacteristic(characteristicUUID)

            if (characteristic != null) {
                // Set the byte array to the characteristic
                characteristic.value = byteArray

                // Write the characteristic
                val result = bluetoothGatt.writeCharacteristic(characteristic, byteArray, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                println("Write characteristic result: $result for characteristic with UUID $characteristicUUID")
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


