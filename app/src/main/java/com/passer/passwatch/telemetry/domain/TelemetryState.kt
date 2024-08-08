package com.passer.passwatch.telemetry.domain

import android.bluetooth.BluetoothGattService

data class TelemetryState(
    val macAddress: String = "",
    val services: List<BluetoothGattService> = emptyList(),
    val characteristicValue : Map<String, String> = hashMapOf()
)
