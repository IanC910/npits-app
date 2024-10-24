package com.passer.passwatch.map.domain

import com.passer.passwatch.core.repo.data.Route
import com.passer.passwatch.nearpass.data.NearPass

data class MapState(
    val rideId: Int = 0,

    val nearPasses: List<NearPass> = emptyList(),
    val routes: List<Route> = emptyList(),
)