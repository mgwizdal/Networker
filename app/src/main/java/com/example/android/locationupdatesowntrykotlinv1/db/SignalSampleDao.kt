package com.example.android.locationupdatesowntrykotlinv1.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface SignalSampleDao {
    @Insert
    fun insert(signalSample: SignalSample)
    @Query("DELETE FROM SignalSample")
    fun delete()
}