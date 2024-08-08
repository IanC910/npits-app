package com.passer.passwatch.nearpass.domain

import com.passer.passwatch.nearpass.data.NearPass

data class NearPassState(
    val rideId : Int = 0,
    val nearPasses : List<NearPass> = emptyList(),

    val latitude: String = "",
    val longitude: String = "",
    val distance: String = "",
    val speed: String = "",
    val time: String = "",
    val isAddingNearPass: Boolean = false,
)
