package com.passer.passwatch

import com.passer.passwatch.model.nearpass.NearPass

data class NearPassState(
    val nearPasses : List<NearPass> = emptyList(),
    val latitude: String = "",
    val longitude: String = "",
    val distance: String = "",
    val speed: String = "",
    val isAddingNearPass: Boolean = false,
)