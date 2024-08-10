package com.passer.passwatch.newride.domain

data class NewRideState(
    val rideStarted: Boolean = false,
    val rideStartTime: Long = 0,
    val rideTime: Long = 0,
)
