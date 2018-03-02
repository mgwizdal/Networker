package com.example.android.locationupdatesowntrykotlinv1

import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * Created by Maks on 2018-02-18.
 */
class MyPhoneListener : PhoneStateListener() {

    var LOG_TAG = "MyPhoneListener"

    var signalStrengthValue: Int = 0
    var signalStrengthRsrp: String = "0"
    var signalStrengthRsrq: String = "0"
    var signalStrengthRssnr: String = "0"

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
        super.onSignalStrengthsChanged(signalStrength)
        try {
            var methods = android.telephony.SignalStrength::class.java.methods
            for (mthd in methods) {
                if (mthd.getName().equals("getLteSignalStrength")
                        || mthd.getName().equals("getLteRsrp")
                        || mthd.getName().equals("getLteRsrq")
                        || mthd.getName().equals("getLteRssnr")
                        || mthd.getName().equals("getLteCqi")) {
                    if(mthd.getName().equals("getLteRsrp")){
                        signalStrengthRsrp = mthd.invoke(signalStrength).toString()
                    } else if(mthd.getName().equals("getLteRsrq")){
                        signalStrengthRsrq = mthd.invoke(signalStrength).toString()
                    }else if(mthd.getName().equals("getLteRssnr")){
                        signalStrengthRssnr = mthd.invoke(signalStrength).toString()
                    }
                    Log.i(LOG_TAG,"onSignalStrengthsChanged: " + mthd.getName() + " " + mthd.invoke(signalStrength))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        // Reflection code ends here
    }
}
