package com.passer.passwatch.ride.domain

import com.passer.passwatch.ride.data.Ride

sealed interface RideEvent {
    data object SaveRide : RideEvent
    data class SetStartTime(val startTime: Long?) : RideEvent
    data class SetEndTime(val endTime: Long?) : RideEvent

    data object ShowDialog : RideEvent
    data object HideDialog : RideEvent

    data class DeleteRide(val ride: Ride) : RideEvent

    data object SyncRides : RideEvent
}