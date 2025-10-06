package com.example.meditrack

import GeoapifyPlacesResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Hide POI and transit labels
        val styleJson = """
            [
              { "featureType": "poi", "elementType": "labels", "stylers": [{ "visibility": "off" }] },
              { "featureType": "transit", "elementType": "labels", "stylers": [{ "visibility": "off" }] }
            ]
        """.trimIndent()
        mMap.setMapStyle(MapStyleOptions(styleJson))

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationAndFetchPlaces()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun enableLocationAndFetchPlaces() {
        try {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                    fetchNearbyPlaces(location.latitude, location.longitude)
                } else {
                    requestFreshLocation()
                }
            }.addOnFailureListener {
                requestFreshLocation()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestFreshLocation() {
        try {
            val locationRequest = LocationRequest.create().apply {
                priority = Priority.PRIORITY_HIGH_ACCURACY
                interval = 10000L
                fastestInterval = 5000L
                numUpdates = 1
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                        fetchNearbyPlaces(location.latitude, location.longitude)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchNearbyPlaces(lat: Double, lng: Double) {
        val apiKey = getString(R.string.geoapify_api_key)
        val categories = "healthcare.hospital,healthcare.pharmacy"
        val filter = "circle:$lng,$lat,4000" // circle:lon,lat,radius

        Log.d("MapActivity", "Fetching places from Geoapify...")

        RetrofitMapsInstance.api.getNearbyPlaces(
            categories = categories,
            filter = filter,
            apiKey = apiKey
        ).enqueue(object : Callback<GeoapifyPlacesResponse> {
            override fun onResponse(
                call: Call<GeoapifyPlacesResponse>,
                response: Response<GeoapifyPlacesResponse>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()?.features ?: emptyList()
                    Log.d("MapActivity", "Places found: ${results.size}")

                    for (feature in results) {
                        val props = feature.properties
                        val coords = feature.geometry.coordinates
                        if (coords.size >= 2) {
                            val latLng = LatLng(coords[1], coords[0]) // [lon, lat] → [lat, lon]

                            val markerColor = when {
                                props.categories?.contains("pharmacy") == true -> BitmapDescriptorFactory.HUE_BLUE
                                props.categories?.contains("hospital") == true -> BitmapDescriptorFactory.HUE_RED
                                else -> BitmapDescriptorFactory.HUE_GREEN
                            }

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(props.name ?: "Unknown")
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            )
                        }
                    }
                } else {
                    val errMsg = response.errorBody()?.string()
                    Log.e("MapActivity", "Geoapify API error: $errMsg")
                    Toast.makeText(this@MapActivity, "Failed: $errMsg", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeoapifyPlacesResponse>, t: Throwable) {
                Log.e("MapActivity", "API call failed", t)
                Toast.makeText(this@MapActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationAndFetchPlaces()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
