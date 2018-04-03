package com.example.android.locationupdatesowntrykotlinv1

import android.app.Application
import android.arch.persistence.room.Room
import com.example.android.locationupdatesowntrykotlinv1.db.SignalSampleDatabase

class App : Application() {
    companion object {
        var database : SignalSampleDatabase? =null
    }

    override fun onCreate() {
        super.onCreate()
        App.database = Room.databaseBuilder(this, SignalSampleDatabase::class.java, "testtest").build()
    }
}