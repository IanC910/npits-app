package com.passer.passwatch

import com.passer.passwatch.model.ride.Ride

sealed interface RideEvent {
    data object SaveRide : RideEvent
    data class SetStartTime(val startTime: Long?) : RideEvent
    data class SetEndTime(val endTime: Long?) : RideEvent

    data object ShowDialog : RideEvent
    data object HideDialog : RideEvent

    data class DeleteRide(val ride: Ride) : RideEvent
}