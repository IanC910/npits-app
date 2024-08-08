package com.passer.passwatch.nearpass.domain

import com.passer.passwatch.nearpass.data.NearPass

sealed interface NearPassEvent {
    data class SetRideId(val rideId: Int) : NearPassEvent

    data object SaveNearPass : NearPassEvent

    data class SetTime(val time: String) : NearPassEvent
    data class SetLatitude(val latitude: String) : NearPassEvent
    data class SetLongitude(val longitude: String) : NearPassEvent
    data class SetDistance(val distance: String) : NearPassEvent
    data class SetSpeed(val speed: String) : NearPassEvent

    data object ShowDialog : NearPassEvent
    data object HideDialog : NearPassEvent

    data class DeleteNearPass(val nearPass: NearPass) : NearPassEvent
}