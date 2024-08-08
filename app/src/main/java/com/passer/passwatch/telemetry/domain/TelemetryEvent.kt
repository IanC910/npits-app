package com.passer.passwatch.telemetry.domain

sealed interface TelemetryEvent {
    data class SetMacAddress(val newMacAddress: String) : TelemetryEvent

    data object ClearServiceCache : TelemetryEvent
    data object ConnectDevice : TelemetryEvent
}