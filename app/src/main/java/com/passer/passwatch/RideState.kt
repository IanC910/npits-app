package com.passer.passwatch

import com.passer.passwatch.model.ride.Ride

data class RideState(
    val rides: List<Ride> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val isAddingRide: Boolean = false,
)