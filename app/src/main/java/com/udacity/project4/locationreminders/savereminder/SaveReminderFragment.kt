package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.persistableBundleOf
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

//import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.jar.Manifest

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var HelperOfGeofence: GeofenceHelper
    private lateinit var ClientOfGeofence: GeofencingClient
    private lateinit var Reminder: ReminderDataItem
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private  val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 35
    private  val FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE = 34
    private  val FINE_LOCATION_PERMISSION_INDEX = 0
    private  val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    private  val FINE_LOCATION_REQUEST_CODE = 33
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(),
            0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

     //   setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        ClientOfGeofence = LocationServices.getGeofencingClient(requireContext())
        HelperOfGeofence = GeofenceHelper(requireContext())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            Reminder = ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(Reminder)) {
                if (FineAndBackgroundLocationPermissionsApproved()) {
              checkDeviceLocationSettingsAndStartGeofence()
                }
                requestFineAndBackgroundLocationPermissions()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun FineAndBackgroundLocationPermissionsApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
                ))

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        //Next, use LocationServices to get the Settings Client and create a val called locationSettingsResponseTask to check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        //Since the case we are most interested in here is finding out if the location settings are not satisfied, add an onFailureListener() to the locationSettingsResponseTask.
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                 startIntentSenderForResult(
                     exception.resolution.intentSender,
                     TURN_DEVICE_LOCATION_ON_REQUEST_CODE,
                     null,
                     0,0,0,null
                 )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
                _viewModel.showErrorMessage.value = R.string.location_required_error.toString()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                StartGeofence()
            }

        }
    }
    @SuppressLint("MissingPermission")
    private fun StartGeofence() {
        val geofence = Geofence.Builder()
            .setRequestId(Reminder.id)
            .setCircularRegion(Reminder.latitude!!,
            Reminder.longitude!!,
                40f
                )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val GeoFenceRequest=GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        ClientOfGeofence.addGeofences( GeoFenceRequest,geofencePendingIntent)?.run{
            addOnSuccessListener {
                _viewModel.saveReminder(Reminder)
            }
            addOnFailureListener {
                _viewModel.showSnackBarInt.value = R.string.error_adding_geofence
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== TURN_DEVICE_LOCATION_ON_REQUEST_CODE){
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ( grantResults.isEmpty() ||
            grantResults[FINE_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        ) {

            _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation

        } else {

            checkDeviceLocationSettingsAndStartGeofence()
        }
    }
@RequiresApi(Build.VERSION_CODES.Q)
fun requestFineAndBackgroundLocationPermissions(){
    if(FineAndBackgroundLocationPermissionsApproved())
        return
    var permissionsArray= arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val requestCode = when {
        runningQOrLater -> {
            permissionsArray += android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            FINE_AND_BACKGROUND_LOCATIONS_REQUEST_CODE
        }
        else -> FINE_LOCATION_REQUEST_CODE
    }

    requestPermissions(permissionsArray, requestCode)
}

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

}



































   /* @SuppressLint("MissingPermission")
    private fun AddGeoFence(Reminder:ReminderDataItem) {

        val latlng = LatLng(Reminder.latitude!!, Reminder.longitude!!)
        geofence = HelperOfGeofence.GetGeofence(
            Reminder.id,
            latlng,
            200f,
            Geofence.GEOFENCE_TRANSITION_ENTER
        )
        RequestOfGeofence = HelperOfGeofence.GetGeogencingRequest(geofence)
        pendingintent = HelperOfGeofence.GetPendingIntent()!!

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("msg", "No permission")
            return
        } else {
        ClientOfGeofence.addGeofences(RequestOfGeofence,pendingintent)
            .addOnSuccessListener {
                Log.d("txt","geofene added succefully")
            }
            .addOnFailureListener { i->
                val Error=HelperOfGeofence.GetErrorString(i)
                _viewModel.showErrorMessage.value=HelperOfGeofence.GetErrorString(i)
                Log.d("txt","geofene Failed")
            }
        }
    }
    fun RemoveGeofences(){
        ClientOfGeofence.removeGeofences(pendingintent)?.run{
            addOnSuccessListener {
                Log.d("txt","Geofence removed succefuuly")
                Toast.makeText(requireContext()," Geofence has been removed",Toast.LENGTH_LONG).show()

                addOnFailureListener {
                    _viewModel.showErrorMessage.value=HelperOfGeofence.GetErrorString(it)
                    Log.d("txt","Geofence removed failed")
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
   /* private fun ValidBackGroundPermission():Boolean{
        var ValidBackGroundPermission=false
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            ValidBackGroundPermission=ContextCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED
        }
        return ValidBackGroundPermission
    }
   private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.cs,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                AddGeoFence(Reminder)
            }
        }
    }**/*/
