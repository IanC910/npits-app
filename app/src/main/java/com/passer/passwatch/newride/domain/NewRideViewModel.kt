package com.passer.passwatch.newride.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewRideViewModel : ViewModel() {
    private val _state = MutableStateFlow(NewRideState())
    val state = _state.asStateFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), NewRideState()
    )

    private var timerJob: Job? = null

    fun onEvent(event: NewRideEvent) {
        when (event) {
            is NewRideEvent.StartRide -> {
                timerJob?.cancel()
                timerJob = viewModelScope.launch {
                    while (true) {
                        delay(1000)
                        _state.update {
                            it.copy(
                                rideTime = it.rideTime.inc()
                            )
                        }
                    }
                }
                _state.update {
                    it.copy(
                        rideStarted = true,
                        rideStartTime = System.currentTimeMillis(),
                        rideTime = 0,
                    )
                }
            }

            is NewRideEvent.StopRide -> {
                timerJob?.cancel()

                _state.update {
                    it.copy(
                        rideStarted = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()

    }
}