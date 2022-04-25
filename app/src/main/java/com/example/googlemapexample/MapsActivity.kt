package com.example.googlemapexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var btnDefaultMap : Button
    private lateinit var btnSatelliteMap : Button
    private lateinit var btnTerrainMap : Button
    private lateinit var btnHybridMap : Button

    private lateinit var map: GoogleMap
//    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapFragment : SupportMapFragment

    private var locationPermissionCode = 1

    // latest location data get
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // interval of fetch location of user
    private lateinit var locationRequest: LocationRequest
    // when device location change that notify us
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        binding = ActivityMapsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
        setContentView(R.layout.activity_maps)

        initView()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
//        mapFragment.getMapAsync(OnMapReadyCallback {
//            map = it
//
//            checkPermissionsAndCurrentLocationAccess()
//
//            val location1 = LatLng(23.02,72.57)
//            map.addMarker(MarkerOptions().position(location1).title("Ahmedabad").icon(
//                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,10f))
//
//            val location2 = LatLng(23.21,72.63)
//            map.addMarker(MarkerOptions().position(location2).title("Gandhinagar"))
//
//            val location3 = LatLng(23.03,72.46)
//            map.addMarker(MarkerOptions().position(location3).title("Bopal"))
//        })

        // current location get
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnSatelliteMap.setOnClickListener {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        btnTerrainMap.setOnClickListener {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }

        btnHybridMap.setOnClickListener {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
        }

        btnDefaultMap.setOnClickListener {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

    }

    private fun initView(){
        btnDefaultMap = findViewById(R.id.btnDefaultMap)
        btnSatelliteMap = findViewById(R.id.btnSatelliteMap)
        btnTerrainMap = findViewById(R.id.btnTerrainMap)
        btnHybridMap = findViewById(R.id.btnHybridMap)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
//        this.map = googleMap
//
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        this.map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        this.map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        map = googleMap

        checkPermissionsAndCurrentLocationAccess()
        getLocationUpdates()
        startLocationUpdates()

        val location1 = LatLng(23.02,72.57)
        map.addMarker(
            MarkerOptions().position(location1).title("Ahmedabad").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,10f))
    }

    private fun checkPermissionsAndCurrentLocationAccess() {
        val permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            // Permission granted
            map.isMyLocationEnabled = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Location Permission Denied by user
                val locationPermissionAlertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                locationPermissionAlertDialog.setTitle(getString(R.string.permission))
                locationPermissionAlertDialog.setMessage(getString(R.string.must_allow_location))
                locationPermissionAlertDialog.setPositiveButton(getString(R.string.open_setting)) { _, _ -> appPermissionSetting() }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                locationPermissionAlertDialog.show()
            } else {
                // User allow Camera permission
                locationPermissionCode = 0
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            }
        }
    }

    private fun showLocationError() {
        Toast.makeText(this, "Please Allow Location Reading", Toast.LENGTH_SHORT).show()
    }

    // App Permission Setting
    private fun appPermissionSetting(){
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${this.packageName}")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User Allow Location Permission
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //  Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                map.isMyLocationEnabled = true
//                getLocationUpdates()
//                startLocationUpdates()
            } else {
                showLocationError()
            }
        }
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    val latLng = LatLng(location.latitude, location.longitude)
//                    val markerOptions = MarkerOptions().position(latLng)
//                    map.addMarker(markerOptions)
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //  Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,mainLooper)
        }
        catch (e: Exception){
            e.printStackTrace()
        }

    }
}