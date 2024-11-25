package com.passer.passwatch.settings.domain

import android.bluetooth.BluetoothDevice

data class SettingsState (
    val hubMacAddress : String = "",
    val newHubMacAddress : String = "",
    val scanning : Boolean = false,
    val scannedDevices : List<BluetoothDevice> = emptyList(),
    val connectionState : String = "Not Connected",
    val syncStatus : String = "",
)
