package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

class GeofenceHelper(base: Context?):ContextWrapper(base) {
    private var pendingIntent:PendingIntent?=null
    private val TAG="GeofenceHelper"

    fun GetGeogencingRequest(geofence:Geofence):GeofencingRequest{
        return GeofencingRequest.Builder().addGeofence(geofence).setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build()
    }
    fun GetGeofence(ID: String?, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setRequestId(ID)
            .setTransitionTypes(transitionTypes)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setLoiteringDelay(4000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }
    fun GetPendingIntent(): PendingIntent? {
        if (pendingIntent != null) return pendingIntent
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, 2777, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }
    fun GetErrorString(e: Exception): String? {
        if (e is ApiException) {
            if(e.statusCode==GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE){
                return "GEOFENCE_NOT_AVAILBLE"
            } else if(e.statusCode==GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ){
                return "GEOFENCE_TOO_MANY_GEOFENCES"
            } else if(e.statusCode== GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS){
                return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            }
        }
        return e.localizedMessage
    }


}