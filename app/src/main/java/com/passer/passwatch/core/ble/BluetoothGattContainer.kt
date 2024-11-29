package com.passer.passwatch.core.ble

import android.bluetooth.BluetoothGatt
import java.util.UUID

object BluetoothGattContainer {
    var gatt: BluetoothGatt? = null
    private var sendQueue: MutableList<Triple<UUID, UUID, ByteArray>> = mutableListOf()

    fun isConnected(): Boolean {
        return gatt != null
    }

    fun emplace(service : UUID, characteristic : UUID, value : ByteArray) {
        sendQueue.add(Triple(service, characteristic, value))
    }

    fun removeLast(){
        sendQueue.removeLast()
    }

    fun clear(){
        sendQueue.clear()
    }

    fun flush(){
        if(sendQueue.isEmpty()) {
            return
        }

        val el = sendQueue.last()
        writeToBluetoothGattCharacteristic(gatt, el.first, el.second, el.third)
    }
}

