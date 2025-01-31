package com.passer.passwatch.ride.domain

import com.passer.passwatch.ride.data.Ride

data class RideState(
    val rides: List<Ride> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val isAddingRide: Boolean = false,
)