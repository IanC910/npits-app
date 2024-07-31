package com.passer.passwatch.model

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.passer.passwatch.model.nearpass.NearPass
import com.passer.passwatch.model.nearpass.NearPassDao
import com.passer.passwatch.model.ride.Ride
import com.passer.passwatch.model.ride.RideDao

@Database(
    entities = [NearPass::class, Ride::class],
    version = 1,
)
abstract class NearPassDatabase : RoomDatabase() {
    abstract val nearPassDao: NearPassDao
    abstract val rideDao: RideDao
}