package com.passer.passwatch.newride.domain

import com.passer.passwatch.core.repo.data.Route
import com.passer.passwatch.nearpass.data.NearPass

data class NewRideState(
    var rideStatusMessage: String = "Not currently in a ride",

    val rideId: Int = 0,

    val rideStarted: Boolean = false,
    val rideStartTime: Long = 0,
    val rideTime: Long = 0,

    val characteristicValue: Map<String, String> = hashMapOf(),
    val nearPasses: List<NearPass> = emptyList(),
    val routes: List<Route> = emptyList(),
)
