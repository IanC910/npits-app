package com.passer.passwatch.settings.domain

sealed interface SettingsEvent {
    data class SetMacAddress(val newMacAddress: String) : SettingsEvent
    data class SaveMacAddress(val newMacAddress: String) : SettingsEvent
}
