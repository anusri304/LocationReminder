package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.ACTION_GEOFENCE_EVENT

/**
 * Users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "GeofenceBroadcastReceiver"
    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        // In the event of error log appropriate geofence error message and return
        if (geofencingEvent.hasError()) {
            val errorMessage = errorMessage(context, geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        // Incase of no errors start the GeofenceTransitionsJobIntentService to do the geofencing.
        if(intent.action == ACTION_GEOFENCE_EVENT) {
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        }

    }
}