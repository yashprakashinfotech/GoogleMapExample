package com.example.googlemapexample

//import android.R
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.directions.route.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {
//class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var btnDefaultMap : Button
    private lateinit var btnSatelliteMap : Button
    private lateinit var btnTerrainMap : Button
    private lateinit var btnHybridMap : Button

    private lateinit var map: GoogleMap

    private lateinit var mapFragment : SupportMapFragment

    private var locationPermissionCode = 1

    // latest location data get
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // interval of fetch location of user
    private lateinit var locationRequest: LocationRequest
    // when device location change that notify us
    private lateinit var locationCallback: LocationCallback

    // Route draw

    private var lineOption : PolylineOptions? = null

    private var myLocation : Location? = null
    private var clickLocation : LatLng? = null
    private var start : LatLng? = null
    private var end : LatLng? = null

    private var location1 : LatLng? = null
    //polyline object
    private var polylines: MutableList<Polyline>? = null
//    private var polyline: List<Polyline>? = null

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

        lineOption = PolylineOptions()
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
        //temporary comment
//        getLocationUpdates()
//        startLocationUpdates()

        location1 = LatLng(23.02,72.57)
        map.addMarker(
            MarkerOptions().position(location1!!).title("Ahmedabad").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location1!!,10f))
    }

    //Route draw action

    //to get user location
    private fun getMyLocation() {
//        map.setMyLocationEnabled(true)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        map.isMyLocationEnabled = true
        map.setOnMyLocationChangeListener(OnMyLocationChangeListener { location ->
//        map.setOnMyLocationChangeListener(OnMyLocationChangeListener { location ->
            myLocation = location
            val ltlng = LatLng(location.latitude, location.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                ltlng, 15f
            )
            map.animateCamera(cameraUpdate)
        })

        //get destination location when user click on map
        map.setOnMapClickListener(OnMapClickListener { latLng ->
            end = latLng
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))

            start = LatLng(myLocation!!.latitude, myLocation!!.longitude)

            // line draw
            lineOption!!.add(start)
            lineOption!!.add(end)
//            lineOption.addAll()
            lineOption!!.width(12F)
            lineOption!!.color(Color.BLUE)
            lineOption!!.geodesic(true)
            map.addPolyline(lineOption!!)

//            val endLatLag = LatLng(latLng.latitude,latLng.longitude)

//            map.setOnMapClickListener {
//                allPoints.add(it)
//                map.clear()
//                map.addMarker(MarkerOptions().position(it))
//            }

//            map.addMarker(MarkerOptions().position(clickLocation!!).title("click").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
            //start route finding
            Findroutes(start, end)
        })
    }

    // function to find Routes.
//    fun Findroutes(Start: LatLng?, End: LatLng?) {
//        if (Start == null || End == null) {
//            Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
//        } else {
//            val routing: Routing = Builder
//                .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(true)
//                .waypoints(Start, End)
//                .key("AIzaSyCGXGYqt3XA5VYxuvTaPEgy7gpX_LOQkuA") //also define your api key here.
//                .build()
//            routing.execute()
//        }
//    }

    private fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {
            val routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .waypoints(end)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyCGXGYqt3XA5VYxuvTaPEgy7gpX_LOQkuA") //also define your api key here.
//                .key("AIzaSyD4uStbluZBnwKADWRtCPalZoddDXdNQbs") //also define your api key here.
                .build()
            routing.execute()

//            lineOption!!.add(start)
//            lineOption!!.add(end)
//            lineOption!!.width(12F)
//            lineOption!!.color(Color.BLUE)
//            lineOption!!.geodesic(true)
//            map.addPolyline(lineOption!!)
////            map.clear()

        }
    }

    //Routing call back functions.
    override fun onRoutingFailure(e: RouteException) {
        val parentLayout: View = findViewById(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
        Log.d("Boss","$snackbar")
        snackbar.show()
//        Findroutes(start,end);
    }

    override fun onRoutingStart() {
        Toast.makeText(this, "Finding Route...", Toast.LENGTH_SHORT).show()
    }

    //If Route finding success..
    override fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
        val center = CameraUpdateFactory.newLatLng(start!!)
        val zoom = CameraUpdateFactory.zoomTo(16f)
        if (polylines != null) {
            polylines!!.clear()
        }
        val polyOptions = PolylineOptions()
        var polylineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        polylines = ArrayList()
        //add route(s) to the map using polyline
        for (i in 0 until route.size) {
            if (i == shortestRouteIndex) {
                polyOptions.color(resources.getColor(R.color.purple_700))
                polyOptions.width(7f)
                polyOptions.addAll(route[shortestRouteIndex].points)
                val polyline: Polyline = map.addPolyline(polyOptions)
                polylineStartLatLng = polyline.points[0]
                val k = polyline.points.size
                polylineEndLatLng = polyline.points[k - 1]
                (polylines as ArrayList<Polyline>).add(polyline)
//                (this.polylines as ArrayList<Polyline>).add(polyline)
            } else {
            }
        }

        //Add Marker on route starting position
        val startMarker = MarkerOptions()
        startMarker.position(polylineStartLatLng!!)
        startMarker.title("My Location")
        map.addMarker(startMarker)

        //Add Marker on route ending position
        val endMarker = MarkerOptions()
        endMarker.position(polylineEndLatLng!!)
        endMarker.title("Destination")
        map.addMarker(endMarker)
    }

    override fun onRoutingCancelled() {
        Findroutes(start, end)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Findroutes(start, end)
    }

    // location permission
    private fun checkPermissionsAndCurrentLocationAccess() {
        val permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            // Permission granted
//            map.isMyLocationEnabled = true
            getMyLocation()
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


//class MapsActivity : FragmentActivity(), OnMapReadyCallback,
//    GoogleApiClient.OnConnectionFailedListener,
//    RoutingListener {
//    //google map object
//    private var mMap: GoogleMap? = null
//
//    //current and destination location objects
//    var myLocation: Location? = null
//    var destinationLocation: Location? = null
//    protected var start: LatLng? = null
//    protected var end: LatLng? = null
//    var locationPermission = false
//
//    //polyline object
//    private var polylines: MutableList<Polyline>? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_maps)
//
//        //request location permission.
//        requestPermision()
//
//        //init google map fragment to show map.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment!!.getMapAsync(this)
//    }
//
//    private fun requestPermision() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
//                LOCATION_REQUEST_CODE
//            )
//        } else {
//            locationPermission = true
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            LOCATION_REQUEST_CODE -> {
//                if (grantResults.size > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                ) {
//                    //if permission granted.
//                    locationPermission = true
//                    getMyLocation()
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return
//            }
//        }
//    }
//
//    //to get user location
//    @SuppressLint("MissingPermission")
//    private fun getMyLocation() {
//        mMap!!.isMyLocationEnabled = true
//        mMap!!.setOnMyLocationChangeListener { location ->
//            myLocation = location
//            val ltlng = LatLng(location.latitude, location.longitude)
//            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
//                ltlng, 10f
//            )
//            mMap!!.animateCamera(cameraUpdate)
//        }
//
//        //get destination location when user click on map
//        mMap!!.setOnMapClickListener { latLng ->
//            end = latLng
//            mMap!!.clear()
//            start = LatLng(myLocation!!.latitude, myLocation!!.longitude)
//            //start route finding
//            Findroutes(start, end)
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//        if (locationPermission) {
//            getMyLocation()
//        }
//    }
//
//    // function to find Routes.
//    fun Findroutes(Start: LatLng?, End: LatLng?) {
//        if (Start == null || End == null) {
//            Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
//        } else {
//            val routing = Routing.Builder()
//                .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(true)
//                .waypoints(Start, End)
//                .key("AIzaSyCGXGYqt3XA5VYxuvTaPEgy7gpX_LOQkuA") //also define your api key here.
//                .build()
//            routing.execute()
//        }
//    }
//
//    //Routing call back functions.
//    override fun onRoutingFailure(e: RouteException) {
//        val parentLayout = findViewById<View>(android.R.id.content)
//        val snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
//        snackbar.show()
//        //        Findroutes(start,end);
//    }
//
//    override fun onRoutingStart() {
//        Toast.makeText(this, "Finding Route...", Toast.LENGTH_LONG).show()
//    }
//
//    //If Route finding success..
//    override fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
//        val center = CameraUpdateFactory.newLatLng(start!!)
//        val zoom = CameraUpdateFactory.zoomTo(16f)
//        if (polylines != null) {
//            polylines!!.clear()
//        }
//        val polyOptions = PolylineOptions()
//        var polylineStartLatLng: LatLng? = null
//        var polylineEndLatLng: LatLng? = null
//        polylines = ArrayList()
//        //add route(s) to the map using polyline
//        for (i in route.indices) {
//            if (i == shortestRouteIndex) {
//                polyOptions.color(resources.getColor(R.color.teal_200))
//                polyOptions.width(7f)
//                polyOptions.addAll(route[shortestRouteIndex].points)
//                val polyline = mMap!!.addPolyline(polyOptions)
//                polylineStartLatLng = polyline.points[0]
//                val k = polyline.points.size
//                polylineEndLatLng = polyline.points[k - 1]
//                (polylines as ArrayList<Polyline>).add(polyline)
//            } else {
//            }
//        }
//
//        //Add Marker on route starting position
//        val startMarker = MarkerOptions()
//        startMarker.position(polylineStartLatLng!!)
//        startMarker.title("My Location")
//        mMap!!.addMarker(startMarker)
//
//        //Add Marker on route ending position
//        val endMarker = MarkerOptions()
//        endMarker.position(polylineEndLatLng!!)
//        endMarker.title("Destination")
//        mMap!!.addMarker(endMarker)
//    }
//
//    override fun onRoutingCancelled() {
//        Findroutes(start, end)
//    }
//
//    override fun onConnectionFailed(connectionResult: ConnectionResult) {
//        Findroutes(start, end)
//    }
//
//    companion object {
//        //to get location permissions.
//        private const val LOCATION_REQUEST_CODE = 23
//    }
//}