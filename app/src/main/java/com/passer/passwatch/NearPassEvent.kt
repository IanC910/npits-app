package com.passer.passwatch

sealed interface NearPassEvent {
    data object SaveNearPass : NearPassEvent

    data class SetLatitude(val latitude: String) : NearPassEvent
    data class SetLongitude(val longitude: String) : NearPassEvent
    data class SetDistance(val distance: String) : NearPassEvent
    data class SetSpeed(val speed: String) : NearPassEvent

    data object ShowDialog : NearPassEvent
    data object HideDialog : NearPassEvent

    data class DeleteNearPass(val nearPass: NearPass) : NearPassEvent
}