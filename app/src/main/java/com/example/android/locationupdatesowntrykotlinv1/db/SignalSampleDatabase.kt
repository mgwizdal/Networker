package com.example.android.locationupdatesowntrykotlinv1.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(SignalSample::class), version = 1)
abstract class SignalSampleDatabase : RoomDatabase() {
    abstract fun signalSampleDao(): SignalSampleDao
}