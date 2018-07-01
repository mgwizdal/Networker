package com.example.android.locationupdatesowntrykotlinv1.downloadSpeed

import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.model.SpeedTestError
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.SpeedTestSocket
import android.os.AsyncTask
import android.util.Log


class SpeedTestTask(var view: SpeedTaskView) : AsyncTask<Void, Void, String>() {
    override fun doInBackground(vararg params: Void): String? {
        val speedTestSocket= SpeedTestSocket()

        speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {

            override fun onCompletion(report: SpeedTestReport) {
                // called when download/upload is finished
                Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.transferRateBit)
                view.onCompletion()
            }

            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                // called when a download/upload error occur
                Log.v("speedtest", "[error]")
                view.onError(speedTestError)
            }

            override fun onProgress(percent: Float, report: SpeedTestReport) {
                // called to notify download/upload progress
                Log.i("speedtest", "[PROGRESS] progress : $percent%")
                Log.i("speedtest" , "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                Log.i("speedtest", "[PROGRESS] rate in bit/s   : " + report.transferRateBit)
                view.onProgress(report.transferRateBit)
            }
        })
        speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/100M.iso")

        return null
    }
}