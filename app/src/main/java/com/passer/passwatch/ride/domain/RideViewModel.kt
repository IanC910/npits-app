package com.passer.passwatch.ride.domain

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.ride.data.Ride
import com.passer.passwatch.ride.data.RideDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RideViewModel(
    private val applicationContext: Context,
    private val rideDao: RideDao,
) : ViewModel() {
    private val _state = MutableStateFlow(RideState())
    private val _rides = rideDao.getRidesOrderedById()

    val state = combine(_state, _rides) { state, rides ->
        state.copy(
            rides = rides
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideState())

    fun onEvent(event: RideEvent){
        when(event){
            is RideEvent.DeleteRide -> {
                viewModelScope.launch {
                    rideDao.deleteRide(event.ride)
                }
            }
            RideEvent.HideDialog -> {
                _state.update {
                    it.copy(
                        isAddingRide = false
                    )
                }
            }
            RideEvent.SaveRide -> {
                val startTime = state.value.startTime
                val endTime = state.value.endTime

                val ride = Ride(
                    startTime = startTime.toLongOrNull(),
                    endTime = endTime.toLongOrNull(),
                )

                viewModelScope.launch {
                    rideDao.insertRide(ride)
                }

                _state.update {
                    it.copy(
                        isAddingRide = false,
                        startTime = "",
                        endTime = "",
                    )
                }
            }
            is RideEvent.SetEndTime -> {
                _state.update {
                    it.copy(endTime = event.endTime.toString())
                }
            }
            is RideEvent.SetStartTime -> {
                _state.update {
                    it.copy(startTime = event.startTime.toString())
                }
            }
            RideEvent.ShowDialog -> {
                _state.update {
                    it.copy(
                        isAddingRide = true
                    )
                }
            }

            RideEvent.SyncRides -> {
                Toast.makeText(applicationContext, "Test", Toast.LENGTH_SHORT).show()

            }
        }
    }
}
