package com.example.android.locationupdatesowntrykotlinv1.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class SignalSample(
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null,
        val latitude : String,
        val longitude :String,
        val time : String,
        val rsrp : String,
        val rsrq : String,
        val rssnr : String,
        val transferRate : String
)