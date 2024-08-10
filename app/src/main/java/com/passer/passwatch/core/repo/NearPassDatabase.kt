package com.passer.passwatch.core.repo

import androidx.room.Database
import androidx.room.RoomDatabase
import com.passer.passwatch.core.repo.data.Route
import com.passer.passwatch.core.repo.data.RouteDao
import com.passer.passwatch.nearpass.data.NearPass
import com.passer.passwatch.nearpass.data.NearPassDao
import com.passer.passwatch.ride.data.Ride
import com.passer.passwatch.ride.data.RideDao

@Database(
    entities = [Ride::class, Route::class, NearPass::class],
    version = 1,
)
abstract class NearPassDatabase : RoomDatabase() {
    abstract val nearPassDao: NearPassDao
    abstract val rideDao: RideDao
    abstract val routeDao: RouteDao
}