package com.passer.passwatch.settings.domain

sealed interface SettingsEvent {
    data class SetMacAddress(val newMacAddress: String) : SettingsEvent
    data class SaveMacAddress(val newMacAddress: String) : SettingsEvent

    data object StartScan : SettingsEvent
    data object StopScan : SettingsEvent

    data object RequestPermissions : SettingsEvent

    data object Connect : SettingsEvent
    data object SyncData : SettingsEvent

    data object LoadGoProCredentials : SettingsEvent
    data class SetGoProWiFi(val newGoProWiFi: String) : SettingsEvent
    data class SetGoProPassword(val newGoProPassword: String) : SettingsEvent
}
