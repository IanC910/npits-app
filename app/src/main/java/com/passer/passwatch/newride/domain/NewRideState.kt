package com.passer.passwatch.newride.domain

data class NewRideState(
    val rideId: Int = 0,

    val rideStarted: Boolean = false,
    val rideStartTime: Long = 0,
    val rideTime: Long = 0,
    val characteristicValue : Map<String, String> = hashMapOf(),

)
