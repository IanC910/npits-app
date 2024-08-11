package com.passer.passwatch.map.domain

sealed interface MapEvent{
    data class SetRideId(val rideId: Int) : MapEvent
}