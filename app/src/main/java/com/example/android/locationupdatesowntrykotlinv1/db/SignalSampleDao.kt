package com.example.android.locationupdatesowntrykotlinv1.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert

@Dao
interface SignalSampleDao {
    @Insert
    fun insert(signalSample: SignalSample)
}