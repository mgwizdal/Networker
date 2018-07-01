package com.example.android.locationupdatesowntrykotlinv1.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration



@Database(entities = arrayOf(SignalSample::class), version = 2)
abstract class SignalSampleDatabase : RoomDatabase() {
    abstract fun signalSampleDao(): SignalSampleDao
}

val migration2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE SignalSample " + " ADD COLUMN transferRate TEXT NOT NULL DEFAULT ''")
    }
}