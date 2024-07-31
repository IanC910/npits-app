package com.passer.passwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.model.nearpass.NearPass
import com.passer.passwatch.model.nearpass.NearPassDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NearPassViewModel(
    private val dao: NearPassDao
) : ViewModel() {

    private val _state = MutableStateFlow(NearPassState())
    private val _nearpasses = dao.getContactsOrderedById()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _nearpasses) { state, nearpasses ->
        state.copy(
            nearPasses = nearpasses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NearPassState())


    fun onEvent(event: NearPassEvent) {
        when (event) {
            NearPassEvent.SaveNearPass -> {
                val latitude = state.value.latitude
                val longitude = state.value.longitude
                val distance = state.value.distance
                val speed = state.value.speed

                val nearPass = NearPass(
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
                    distance = distance.toDouble(),
                    speed = speed.toDouble(),
                )

                viewModelScope.launch {
                    dao.insertNearPass(nearPass)
                }

                _state.update { it.copy(
                    isAddingNearPass = false,
                    latitude = "",
                    longitude = "",
                    distance = "",
                    speed = ""
                ) }

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

            NearPassEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingNearPass = true
                    )
                }
            }

            NearPassEvent.HideDialog -> {
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
        }
    }
}