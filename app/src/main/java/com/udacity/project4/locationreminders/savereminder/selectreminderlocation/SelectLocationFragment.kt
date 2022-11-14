package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsClient.getPackageName
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.String
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val INITIAL_ZOOM = 12f
        private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val FOREGROUND_LOCATION_PERMISSION_INDEX = 0
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var mMap: GoogleMap? = null
    private var marker: Marker? = null
    private var lastKnownLocation: Location? = null

    //    zoom to the user location after taking his permission
    var home = LatLng(-29.890790, 30.929810)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        //    add the map setup implementation
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //https://stackoverflow.com/questions/36618154/android-supportmapfragment-inside-fragment
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkLocationPermission()

        binding.saveLocationButton.setOnClickListener {
            if (marker == null) {
                Toast.makeText(activity, getString(R.string.select_poi), Toast.LENGTH_LONG).show()
            } else {
                onLocationSelected()
            }
        }

        return binding.root
    }

    // Checking the location permission and the device location settings one after another.
    private fun checkLocationPermission() {
        if (foregroundPermissionApproved()) {
            checkDeviceLocationSettings()
        } else {
            requestForegroundLocationPermissions()
        }
    }

    @TargetApi(29)
    private fun requestForegroundLocationPermissions() {
        if (foregroundPermissionApproved()) {
            return
        }
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(
            permissionsArray,
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        )

    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        // Check the location settings to check if it has changed
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE, null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        TAG,
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    @TargetApi(29)
    private fun foregroundPermissionApproved(): Boolean {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }
    /*
     When the user confirms on the selected location,
      send back the selected location details to the view model
       and navigate back to the previous fragment to save the reminder and add the geofence
     */

    private fun onLocationSelected() {
        marker?.let {
            _viewModel.reminderSelectedLocationStr.value = it.title
            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude
        }

        findNavController().popBackStack()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //  Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        mMap?.let { setMapLongClick(it) } // Set a long click listener for the map;

        mMap?.let { setPoiClick(it) } // Set a click listener for points of interest.

        setMapStyle(mMap) // Set the custom map style.

        mMap?.let { showMyLocation(it) } // Enable location tracking.


    }

    //   add style to the map from the custom xml
    private fun setMapStyle(mMap: GoogleMap?) {
        mMap?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style
            )
        )
    }

    /**
     * Adds a blue marker to the map when the user long clicks on it.
     *
     * @param map The GoogleMap to attach the listener to.
     */
    private fun setMapLongClick(map: GoogleMap) {

        // Add a blue marker to the map when the user performs a long click.
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            // marker.showInfoWindow()
        }
    }

    /**
     * Adds a marker when a place of interest (POI) is clicked with the name of
     * the POI and immediately shows the info window.
     *
     * @param map The GoogleMap to attach the listener to.
     */
    // put a marker to location that the user selected
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker?.showInfoWindow()
            marker?.tag = getString(R.string.poi)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showMyLocation(map: GoogleMap) {
        try {
            if (foregroundPermissionApproved()) {
                map.isMyLocationEnabled = true
                displayCurrentLocation()
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#get-the-location-of-the-android-device-and-position-the-map
    private fun displayCurrentLocation() {
        try {
            if (foregroundPermissionApproved()) {
                val lastLocationResult = fusedLocationProviderClient.lastLocation
                lastLocationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            // Zoom the camera to the current user location
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), INITIAL_ZOOM
                                )
                            )
                        }
                    } else {
                        // Zoom the camera to the default location
                        mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(home, INITIAL_ZOOM)
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<kotlin.String>,
        grantResults: IntArray
    ) {
        if (foregroundPermissionApproved()) {
            displayCurrentLocation()
            checkDeviceLocationSettings()
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        requestForegroundLocationPermissions()
                    }.show()

            } else {
                displayAlertDialog()
            }
        }
    }

    fun displayAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireActivity())

        alertDialogBuilder.setMessage(R.string.manual_enable_while_use_permission)
        alertDialogBuilder.setPositiveButton(
            "OK"
        ) { arg0, arg1 -> requireActivity().finish() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun launchSettingsIntent() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    // Check if the user has accepted the permission. If not prompt again
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettings(false)
        }
    }
}
