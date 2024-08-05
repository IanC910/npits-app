package com.passer.passwatch.settings.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    bluetoothManager: BluetoothManager
) : ViewModel() {
    private val bleScanner = bluetoothManager.adapter.bluetoothLeScanner

    private val _state = MutableStateFlow(SettingsState())
    private val _hubMacAddress = userPreferencesRepository.hubMacAddress
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())

    val state = combine(_state, _hubMacAddress, _scannedDevices) { state, hubMacAddress, scannedDevices ->
        state.copy(
            hubMacAddress = hubMacAddress,
            scannedDevices = scannedDevices
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())


    @SuppressLint("MissingPermission")
    fun onEvent(event: SettingsEvent){
        when(event){
            is SettingsEvent.SaveMacAddress -> {
                val newMacAddress = event.newMacAddress

                viewModelScope.launch {
                    userPreferencesRepository.saveHubMacAddress(newMacAddress)
                }

                _state.update {
                    it.copy(newHubMacAddress = "")
                }
            }

            is SettingsEvent.SetMacAddress -> {
                _state.update {
                    it.copy(newHubMacAddress = event.newMacAddress)
                }
            }

            is SettingsEvent.StartScan -> {
                viewModelScope.launch {
                    val scanSettings: ScanSettings = ScanSettings.Builder()
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build()

                    bleScanner.startScan(null, scanSettings, scanCallback)
                }
            }

            is SettingsEvent.StopScan -> {
                viewModelScope.launch {
                    bleScanner.stopScan(scanCallback)
                }

                _scannedDevices.update { emptyList() }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!state.value.scannedDevices.contains(device)) {
                _scannedDevices.update { it + device }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.i("BleScannerViewModel", "onScanFailed: $errorCode")
            super.onScanFailed(errorCode)
        }
    }
}


