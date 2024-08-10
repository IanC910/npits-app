package com.passer.passwatch.newride.domain

sealed interface NewRideEvent{
    data object StartRide : NewRideEvent
    data object StopRide : NewRideEvent
    data object RequestPermissions : NewRideEvent
}