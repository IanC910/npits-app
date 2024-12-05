package com.passer.passwatch.map.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.repo.data.RouteDao
import com.passer.passwatch.nearpass.data.NearPassDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(routeDao: RouteDao, nearPassDao: NearPassDao) : ViewModel() {
    private val _state = MutableStateFlow(MapState())

    private val _nearpasses = _state.flatMapLatest {
        nearPassDao.getNearPassesForRide(it.rideId)
    }
    private val _routes = _state.flatMapLatest {
        routeDao.getRoutesForRide(it.rideId)
    }

    val state = combine(_state, _nearpasses, _routes) { state, nearpasses, routes ->
        state.copy(
            nearPasses = nearpasses,
            routes = routes
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), MapState()
    )

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.SetRideId -> {
                _state.value = _state.value.copy(
                    rideId = event.rideId
                )
            }
        }
    }
}