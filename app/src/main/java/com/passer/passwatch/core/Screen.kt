package com.passer.passwatch.core

import kotlinx.serialization.Serializable


@Serializable
data object MainMenuScreen

@Serializable
data object NewRideScreen

@Serializable
data object RidesScreen

@Serializable
data class MapScreen(
    val rideId: Int,
)

@Serializable
data object SettingsScreen

@Serializable
data class NearPassScreen(
    val rideId: Int,
)

@Serializable
data class TelemetryScreen(
    val macAddress: String,
)