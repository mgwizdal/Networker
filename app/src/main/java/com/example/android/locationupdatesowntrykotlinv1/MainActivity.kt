package com.example.android.locationupdatesowntrykotlinv1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.telephony.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private val REQUEST_CHECK_SETTINGS = 0x1
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private val KEY_LOCATION = "location"
    private val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    lateinit private var mLocationRequest: LocationRequest
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null


    lateinit private var mStartUpdatesButton: Button
    lateinit private var mStopUpdatesButton: Button
    lateinit private var mLastUpdateTimeTextView: TextView
    lateinit private var mLatitudeTextView: TextView
    lateinit private var mLongitudeTextView: TextView

    private var mRequestingLocationUpdates: Boolean = false

    private var mLastUpdateTime: String? = null

    //changes
    lateinit private var mSignalStrengthLteRsrpTextView: TextView
    lateinit private var mSignalStrengthLteRsrqTextView: TextView
    lateinit private var mSignalStrengthLteRssnrTextView: TextView
    private var newLineString = "\n"

    var mTelephonyManager: TelephonyManager? = null
    var mPhoneStatelistener = MyPhoneListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mStartUpdatesButton = findViewById(R.id.start_updates_button)
        mStopUpdatesButton = findViewById(R.id.stop_updates_button)
        mLatitudeTextView = findViewById(R.id.latitude_text)
        mLongitudeTextView = findViewById(R.id.longitude_text)
        mLastUpdateTimeTextView = findViewById(R.id.last_update_time_text)

        //changes
        mSignalStrengthLteRsrpTextView = findViewById(R.id.signal_strength_rsrp)
        mSignalStrengthLteRsrqTextView = findViewById(R.id.signal_strength_rsrq)
        mSignalStrengthLteRssnrTextView = findViewById(R.id.signal_strength_rssnr)

        mTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        mTelephonyManager?.listen(mPhoneStatelistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        mLastUpdateTime = ""
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)


        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                mCurrentLocation = locationResult.lastLocation
                var sdf = SimpleDateFormat("HH:mm")
                mLastUpdateTime = sdf.format(Date())
                updateLocationUI()
            }

        }
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(TAG, "User agreed to make required location settings changes.")
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }// Nothing to do. startLocationupdates() gets called in onResume again.
        }
    }

    fun startUpdatesButtonHandler(view: View) {
        if (mRequestingLocationUpdates == false) {
            mRequestingLocationUpdates = true
            setButtonsEnabledState()
            startLocationUpdates()
        }
    }

    fun stopUpdatesButtonHandler(view: View) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient?.checkLocationSettings(mLocationSettingsRequest)
                ?.addOnSuccessListener(this) {
                    Log.i(TAG, "All location settings are satisfied.")

                    try {

                        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Security Exception!!!! : " + e.stackTrace)
                    }

                    updateUI()
                }
                ?.addOnFailureListener(this) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)
                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                            mRequestingLocationUpdates = false
                        }
                    }

                    updateUI()
                }
    }

    private fun updateUI() {
        setButtonsEnabledState()
        updateLocationUI()
    }

    private fun setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.isEnabled = false
            mStopUpdatesButton.isEnabled = true
        } else {
            mStartUpdatesButton.isEnabled = true
            mStopUpdatesButton.isEnabled = false
        }
    }

    // metody allcellinfo, neighbouring cell info, celllocation zawiodly, jedynie metoda PhoneStateListenera zadziałało, z refleksją
    private fun updateLocationUI() {
        //changes
        if (mCurrentLocation != null) {


            mLatitudeTextView.append(mCurrentLocation?.latitude.toString())
            mLongitudeTextView.append(mCurrentLocation?.longitude.toString())
            mLastUpdateTimeTextView.append(mLastUpdateTime.toString())
            if(!mPhoneStatelistener.signalStrengthRsrp.equals("0")  ){
                mSignalStrengthLteRsrpTextView.append(mPhoneStatelistener.signalStrengthRsrp)
                Log.i("MainAct", "setting signal changes" +mPhoneStatelistener.signalStrengthRsrp)
                mSignalStrengthLteRsrqTextView.append(mPhoneStatelistener.signalStrengthRsrq)
                Log.i("MainAct", "setting signal changes" +mPhoneStatelistener.signalStrengthRsrq)
                mSignalStrengthLteRssnrTextView.append(mPhoneStatelistener.signalStrengthRssnr)
                Log.i("MainAct", "setting signal changes" +mPhoneStatelistener.signalStrengthRssnr)
            }
            newLine()
        }
    }

    private fun newLine(){
        mLatitudeTextView.append(newLineString)
        mLongitudeTextView.append(newLineString)
        mLastUpdateTimeTextView.append(newLineString)
        mSignalStrengthLteRsrpTextView.append(newLineString)
        mSignalStrengthLteRsrqTextView.append(newLineString)
        mSignalStrengthLteRssnrTextView.append(newLineString)
    }
    private fun stopLocationUpdates() {
        if (mRequestingLocationUpdates == false) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
                ?.addOnCompleteListener(this) {
                    mRequestingLocationUpdates = false
                    setButtonsEnabledState()
                }
    }

    public override fun onResume() {
        super.onResume()
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates()
        } else if (!checkPermissions()) {
            requestPermissions()
        }

        updateUI()
    }

    override fun onPause() {
        super.onPause()

        // Remove location updates to save battery.
        stopLocationUpdates()
    }

    /**
     * Stores activity data in the Bundle.
     */
    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState!!.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates)
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation)
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show()
    }

    /**
     * Return the current state of the permissions needed.
     */
     fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, View.OnClickListener {
                // Request permission
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE)
            })
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates")
                    startLocationUpdates()
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, View.OnClickListener {
                    // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package",
                            BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                })
            }
        }
    }
}
