package com.passer.passwatch.core.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import java.util.UUID

object BluetoothGattContainer {
    var gatt: BluetoothGatt? = null
    private var sendQueue: MutableList<Triple<UUID, UUID, ByteArray>> = mutableListOf()
    private var sendDescriptorQueue: MutableList<BluetoothGattCharacteristic> = mutableListOf()

    fun isConnected(): Boolean {
        return gatt != null
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        sendQueue.clear()
        sendDescriptorQueue.clear()
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

    fun isDescriptorQueueEmpty(): Boolean {
        return sendDescriptorQueue.isEmpty()
    }

    fun emplaceDescriptor(characteristic : BluetoothGattCharacteristic) {
        sendDescriptorQueue += characteristic
    }

    fun removeLastDescriptor(){
        sendDescriptorQueue.removeLast()
    }

    fun clearDescriptors(){
        sendDescriptorQueue.clear()
    }

    fun flushDescriptors(){
        if(sendDescriptorQueue.isEmpty()) {
            Log.i("BluetoothGattContainer", "Descriptor queue is empty")
            return
        }

        if(gatt == null) {
            Log.e("BluetoothGattContainer", "Gatt is null, cannot write descriptors")
            return
        }

        val characteristic: BluetoothGattCharacteristic = sendDescriptorQueue.last()

        if (!gatt!!.setCharacteristicNotification(characteristic, true)) {
            Log.e(
                "BluetoothGattContainer",
                "Characteristic ${characteristic.uuid} notification set failed"
            )
        } else {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            Log.i(
                "BluetoothGattContainer",
                "Characteristic ${characteristic.uuid} notification set"
            )

            val descriptor = characteristic.getDescriptor(characteristic.descriptors[0].uuid)

            if (descriptor != null) {
                val ret = gatt!!.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)

                Log.i("BluetoothGattContainer", "Write descriptor returned $ret, ${sendDescriptorQueue.size} elements left to process")

            } else {
                Log.e(
                    "BluetoothGattContainer",
                    "CCCD descriptor not found for characteristic ${characteristic.uuid}"
                )
            }
        }
    }
}

