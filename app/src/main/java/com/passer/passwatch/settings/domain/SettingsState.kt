package com.passer.passwatch.settings.domain

import android.bluetooth.BluetoothDevice

data class SettingsState (
    val hubMacAddress : String = "",
    val newHubMacAddress : String = "",
    val scannedDevices : List<BluetoothDevice> = emptyList()
)
