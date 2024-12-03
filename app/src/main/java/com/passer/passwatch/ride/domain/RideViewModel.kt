package com.passer.passwatch.ride.domain

import android.Manifest
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.ble.BluetoothGattContainer
import com.passer.passwatch.core.ble.UUIDConstants
import com.passer.passwatch.core.util.convertToBytes
import com.passer.passwatch.core.util.epochToUTC
import com.passer.passwatch.nearpass.data.NearPassDao
import com.passer.passwatch.ride.data.Ride
import com.passer.passwatch.ride.data.RideDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class RideViewModel(
    private val applicationContext: Context,
    private val rideDao: RideDao,
    private val nearPassDao: NearPassDao,
) : ViewModel() {
    private val _state = MutableStateFlow(RideState())
    private val _rides = rideDao.getRidesOrderedById()
    private val _permissionNeeded = MutableSharedFlow<String>()

    val state = combine(_state, _rides) { state, rides ->
        state.copy(
            rides = rides
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RideState())

    fun onEvent(event: RideEvent){
        when(event){
            RideEvent.RequestPermissions -> {
                viewModelScope.launch {
                    _permissionNeeded.emit(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    _permissionNeeded.emit(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                }
            }

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
                if(!BluetoothGattContainer.isConnected()) {
                    Toast.makeText(applicationContext, "Connect to a device first!", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(applicationContext, "Syncing Rides", Toast.LENGTH_SHORT).show()


                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        rideDao.deleteAllRides() // Suspend until deletion is complete
                    }

                    BluetoothGattContainer.emplace(
                        UUIDConstants.SERVICE_RIDES_LIST.uuid,
                        UUIDConstants.RL_REQUEST.uuid,
                        convertToBytes(1)
                    )

                    BluetoothGattContainer.flush()
                }
            }

            RideEvent.ExportCSV -> {
                Log.i("RideViewModel", "Exporting CSV")

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val filePath = File(downloadsDir, "PassWatch_export.csv").absolutePath

                Log.i("RideViewModel", "File path: $filePath")

                val writer: BufferedWriter = Files.newBufferedWriter(Paths.get(filePath))
                val csvPrinter = CSVPrinter(writer, CSVFormat.Builder.create().setHeader("Latitude", "Longitude", "Distance", "Speed", "Time", "Ride ID").build())

                viewModelScope.launch {
                    nearPassDao.allNearPasses().forEach{ np ->
                        csvPrinter.printRecord(
                            np.latitude,
                            np.longitude,
                            np.distance,
                            np.speed,
                            epochToUTC(np.time!! / 1000),
                            np.rideId
                        )

                        Log.i("RideViewModel", "Appended NP: $np")
                    }

                    csvPrinter.flush()
                    csvPrinter.close()

                    Log.i("RideViewModel", "CSV exported")
                    Toast.makeText(applicationContext, "CSV exported", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
