package com.passer.passwatch.core.ble

import android.bluetooth.BluetoothGatt

object BluetoothGattContainer {
    var gatt: BluetoothGatt? = null

    fun isConnected(): Boolean {
        return gatt != null
    }
}