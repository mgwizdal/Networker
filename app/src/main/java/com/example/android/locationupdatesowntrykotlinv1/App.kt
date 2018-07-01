package com.example.android.locationupdatesowntrykotlinv1

import android.app.Application
import android.arch.persistence.room.Room
import com.example.android.locationupdatesowntrykotlinv1.db.SignalSampleDatabase
import com.example.android.locationupdatesowntrykotlinv1.db.migration2
import com.facebook.stetho.Stetho

class App : Application() {
    companion object {
        var database : SignalSampleDatabase? =null
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        App.database = Room.databaseBuilder(this, SignalSampleDatabase::class.java, "testtest")
                .addMigrations(migration2)
                .build()
    }
}