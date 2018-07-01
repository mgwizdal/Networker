package com.example.android.locationupdatesowntrykotlinv1.downloadSpeed

import fr.bmartel.speedtest.model.SpeedTestError
import java.math.BigDecimal

interface SpeedTaskView{
    fun onError(speedTestError: SpeedTestError)
    fun onCompletion()
    fun onProgress(transferRate: BigDecimal )
}