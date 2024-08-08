package com.passer.passwatch.settings.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
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
    bluetoothManager: BluetoothManager,
) : ViewModel() {
    private val scanPeriod: Long = 10000
    private val bleScanner = bluetoothManager.adapter.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(SettingsState())
    private val _hubMacAddress = userPreferencesRepository.hubMacAddress

    val state = combine(_state, _hubMacAddress) { state, hubMacAddress ->
        state.copy(
            hubMacAddress = hubMacAddress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())


    @SuppressLint("MissingPermission")
    fun onEvent(event: SettingsEvent) {
        when (event) {
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


                    handler.postDelayed({
                        onEvent(SettingsEvent.StopScan)
                    }, scanPeriod)

                    _state.update {
                        it.copy(
                            scannedDevices = emptyList(),
                            scanning = true
                        )
                    }

                    bleScanner.startScan(null, scanSettings, scanCallback)
                }
            }

            is SettingsEvent.StopScan -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            scanning = false
                        )
                    }

                    bleScanner.stopScan(scanCallback)
                }
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!state.value.scannedDevices.contains(device)) {
                _state.update {
                    it.copy(scannedDevices = _state.value.scannedDevices + device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.i("BleScannerViewModel", "onScanFailed: $errorCode")
            super.onScanFailed(errorCode)
        }
    }
}


