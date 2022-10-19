package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.BuildConfig.APPLICATION_ID
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_select_location.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val TAG=SelectLocationFragment::class.java.simpleName
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
 private  var LocationPermission=false;
    private var lastLocation:Location?=null
    private lateinit var currentLocation:FusedLocationProviderClient
private var DefualtLocation=LatLng(-33.852,151.211) //Syndy
    private lateinit var pointOfInterest: PointOfInterest
private var marke:Marker?=null
    private  val FINE_LOCATION_PERMISSION_REQUEST_CODE = 1
    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        View_of_map.onCreate(savedInstanceState)
        View_of_map.onResume()
        View_of_map.getMapAsync(this)

    }*/
    enum class MapZoomLevel(val level:Float){
        World(1f),
        Landmass(5f),
        City(10f),
        Streets(15f),
        Buildings(20f)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.View_of_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
         binding.save.setOnClickListener {
             onLocationSelected()
         }

        return binding.root
    }

    private fun onLocationSelected() {
       marke?.let{
           _viewModel.reminderSelectedLocationStr.value=it.title
           _viewModel.latitude.value=it.position.latitude
           _viewModel.longitude.value=it.position.longitude
       }
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //checkPermissions()
        map.setOnMapClickListener { latlng->
            addMapMarker(latlng)
            marke!!.showInfoWindow()

        }
        map.setOnPoiClickListener { poi ->
            addPoiMarker(poi)
            marke!!.showInfoWindow()

        }
        setMapStyle(map)
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getMyLocation()

        }else {

            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION_REQUEST_CODE)
        }



    }
    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
      map.isMyLocationEnabled=true
        AddLocation()
    }
    @SuppressLint("MissingPermission")
    private fun AddLocation() {
        val fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireContext())
        val getLastLocation=fusedLocationProviderClient.lastLocation
        getLastLocation.addOnCompleteListener(requireActivity()){
            task->
            if(task.isSuccessful){
                val Result=task.result
                Result?.run {
                    val latlng=LatLng(latitude,longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latlng,MapZoomLevel.Streets.level)
                    )
                    addMapMarker(latlng)
                }
            }
        }
    }

    fun addMapMarker(latlng: LatLng){
        marke?.remove()
        marke=map.addMarker(MarkerOptions()
            .position(latlng)
            .title(getString(R.string.dropped_pin))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }
    private fun setMapStyle(map: GoogleMap){
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
                    R.raw.map_file
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==FINE_LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty()&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getMyLocation()
            }
            else{
                _viewModel.showSnackBarInt.value=R.string.permission_denied_explanation
            }
        }
    }
    private fun addPoiMarker(poi: PointOfInterest) {
        marke?.remove()
        marke = map.addMarker(MarkerOptions()
            .position(poi.latLng)
            .title(poi.name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }
    //----------------------------------------------Map long click and Style------------------------------------------------//
   /* private fun onLocationSelected() {

       /* _viewModel.latitude.postValue(latLng.latitude)
        _viewModel.longitude.postValue(latLng.longitude)

        val fromLocation=Geocoder(activity).getFromLocation(latLng.latitude,latLng.longitude,2)
        _viewModel.reminderSelectedLocationStr.postValue(fromLocation[0].locality)
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)*/
        marke?.let{
            _viewModel.reminderSelectedLocationStr.value=it.title
            _viewModel.latitude.value=it.position.latitude
            _viewModel.longitude.value=it.position.longitude
        }
        findNavController().popBackStack()
    }
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
      //      onLocationSelected(latLng)
        }
    }
   private fun setPoiClick(poi: PointOfInterest) {
     marke?.remove()
       marke=map.addMarker(MarkerOptions().position(poi.latLng)
           .title(poi.name)
           .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
       )
    }
    fun AddCircle(latLng: LatLng,radius:Float){
        val circleOptions=CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255,255,0,0))
        circleOptions.fillColor(Color.argb(64,255,0,0))
        circleOptions.strokeWidth(4f)
        map.addCircle(circleOptions)
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
                    R.raw.map_file
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }
//----------------------------------------------------------Permission--------------------------------------------------------------------------------//
    private fun checkPermissions(){
        if(foregroundAndBackgroundLocationPermissionApproved()){
            checkDeviceLocationSettingsAndStartGeofence()
        }
        else{
            requestForegroundAndBackgroundLocationPermissions()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if(grantResults.isEmpty()||grantResults[LOCATION_PERMISSION_INDEX]==PackageManager.PERMISSION_DENIED||
            (  requestCode== REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE&&grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX]==PackageManager.PERMISSION_DENIED)){
            Snackbar.make(
                binding.Container,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings){
                    startActivity(Intent().apply {
                        action=Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data=Uri.fromParts("package", BuildConfig.APPLICATION_ID,null)
                        flags=Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            _viewModel.showErrorMessage.value=R.string.permission_denied_explanation.toString()
        }
        else{
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }
@TargetApi(29)
fun foregroundAndBackgroundLocationPermissionApproved():Boolean{
    val foregroundLocationApproved=(
            PackageManager.PERMISSION_GRANTED==
                    ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
            )
    val backgroundPermissionApproved=
        if(runningQOrLater){
            PackageManager.PERMISSION_GRANTED==
                    ActivityCompat.checkSelfPermission(
                        requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        }
        else{
            true
        }
    return foregroundLocationApproved&&backgroundPermissionApproved

}
    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        // TODO: Step 4 add code to request foreground and background permissions
        if(foregroundAndBackgroundLocationPermissionApproved())
            return
        //add ACCESS_FINE_LOCATION(location) since that will be needed on all API levels.
        var permissionsArray= arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode=when{
            runningQOrLater->{
                permissionsArray+=Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE

            }
            else-> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        activity?.let {
            ActivityCompat.requestPermissions(
                it, permissionsArray, resultCode
            )
        }
    }


    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        //Next, use LocationServices to get the Settings Client and create a val called locationSettingsResponseTask to check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        //Since the case we are most interested in here is finding out if the location settings are not satisfied, add an onFailureListener() to the locationSettingsResponseTask.
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.Container,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
                _viewModel.showErrorMessage.value=R.string.location_required_error.toString()
            }
        }
             locationSettingsResponseTask.addOnCompleteListener{
                 if (it.isSuccessful) {
                     map.isMyLocationEnabled = true
                     Toast.makeText(
                         requireContext(),
                         getString(R.string.long_click),
                         Toast.LENGTH_LONG
                     ).show()
                 }

            }
        }


    companion object{
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }*/
}
