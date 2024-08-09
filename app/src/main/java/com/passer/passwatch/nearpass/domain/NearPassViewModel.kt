package com.passer.passwatch.nearpass.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.nearpass.data.NearPass
import com.passer.passwatch.nearpass.data.NearPassDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NearPassViewModel(
    private val dao: NearPassDao,
) : ViewModel() {

    private val _state = MutableStateFlow(NearPassState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _nearpasses = _state.flatMapLatest { it ->
        dao.getNearPassesForRide(it.rideId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _nearpasses) { state, nearpasses ->
        state.copy(
            nearPasses = nearpasses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NearPassState())


    fun onEvent(event: NearPassEvent) {
        when (event) {
            is NearPassEvent.SaveNearPass -> {
                val rideId = state.value.rideId
                val latitude = state.value.latitude
                val longitude = state.value.longitude
                val distance = state.value.distance
                val speed = state.value.speed
                val time = state.value.time

                val nearPass = NearPass(
                    rideId = rideId,
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
                    distance = distance.toDouble(),
                    speed = speed.toDouble(),
                    time = time.toLong(),
                )

                viewModelScope.launch {
                    dao.insertNearPass(nearPass)
                }

                _state.update {
                    it.copy(
                        isAddingNearPass = false,
                        latitude = "",
                        longitude = "",
                        distance = "",
                        speed = ""
                    )
                }

            }

            is NearPassEvent.SetTime -> {
                _state.update {
                    it.copy(time = event.time)
                }
            }

            is NearPassEvent.SetLatitude -> {
                _state.update {
                    it.copy(latitude = event.latitude)
                }
            }

            is NearPassEvent.SetLongitude -> {
                _state.update {
                    it.copy(longitude = event.longitude)
                }
            }

            is NearPassEvent.SetDistance -> {
                _state.update {
                    it.copy(distance = event.distance)
                }
            }

            is NearPassEvent.SetSpeed -> {
                _state.update {
                    it.copy(speed = event.speed)
                }
            }

            is NearPassEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingNearPass = true
                    )
                }
            }

            is NearPassEvent.HideDialog -> {
                _state.update {
                    it.copy(
                        isAddingNearPass = false
                    )
                }
            }

            is NearPassEvent.DeleteNearPass -> {
                viewModelScope.launch {
                    dao.deleteNearPass(event.nearPass)
                }
            }

            is NearPassEvent.SetRideId -> {
                _state.update {
                    it.copy(
                        rideId = event.rideId
                    )
                }
            }
        }
    }
}