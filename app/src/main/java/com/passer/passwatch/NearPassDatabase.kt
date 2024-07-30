package com.passer.passwatch

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NearPass::class],
    version = 1
)
abstract class NearPassDatabase : RoomDatabase() {
    abstract val dao: NearPassDao
}