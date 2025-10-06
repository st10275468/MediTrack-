package com.example.meditrack

import Center
import Circle
import LocationRestriction
import NearbySearchRequest
import NearbySearchResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

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
    private var hasCenteredOnResults = false

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
            Log.d("MapActivity", "Requesting last location...")

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("MapActivity", "Location obtained: ${location.latitude}, ${location.longitude}")
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                    fetchNearbyPlaces(location.latitude, location.longitude)
                } else {
                    Log.e("MapActivity", "Location is NULL, requesting fresh location...")
                    requestFreshLocation()
                }
            }.addOnFailureListener { e ->
                Log.e("MapActivity", "Failed to get location", e)
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                // Try requesting fresh location as fallback
                requestFreshLocation()
            }
        } catch (e: SecurityException) {
            Log.e("MapActivity", "Security exception", e)
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestFreshLocation() {
        try {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000L
                fastestInterval = 5000L
                numUpdates = 1
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        Log.d("MapActivity", "Fresh location obtained: ${location.latitude}, ${location.longitude}")
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                        fetchNearbyPlaces(location.latitude, location.longitude)

                        fusedLocationClient.removeLocationUpdates(this)
                    } ?: run {
                        Log.e("MapActivity", "Fresh location result is null")
                        Toast.makeText(this@MapActivity, "Unable to get current location. Please ensure location services are enabled.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("MapActivity", "Security exception requesting location updates", e)
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchNearbyPlaces(lat: Double, lng: Double) {
        val apiKey = getString(R.string.google_api_key)

        val typeMapping = mapOf(
            "pharmacy" to "pharmacy",
            "hospital" to "hospital",
            "doctor" to "doctor"
        )

        typeMapping.forEach { (oldType, newType) ->
            val request = NearbySearchRequest(
                includedTypes = listOf(newType),
                maxResultCount = 20,
                locationRestriction = LocationRestriction(
                    circle = Circle(
                        center = Center(latitude = lat, longitude = lng),
                        radius = 2000.0
                    )
                )
            )

            Log.d("MapActivity", "Searching for $newType near $lat,$lng")

            RetrofitMapsInstance.api.searchNearbyPlaces(
                apiKey = apiKey,
                request = request
            ).enqueue(object : Callback<NearbySearchResponse> {
                override fun onResponse(
                    call: Call<NearbySearchResponse>,
                    response: Response<NearbySearchResponse>
                ) {
                    Log.d("MapActivity", "Response for $oldType: Success=${response.isSuccessful}, Code=${response.code()}")

                    if (response.isSuccessful) {
                        val places = response.body()?.places
                        Log.d("MapActivity", "Found ${places?.size ?: 0} $oldType places")

                        places?.forEach { place ->
                            val name = place.displayName?.text ?: "Unknown"
                            val latLng = LatLng(place.location.latitude, place.location.longitude)

                            val markerColor = when (oldType) {
                                "pharmacy" -> BitmapDescriptorFactory.HUE_BLUE
                                "hospital" -> BitmapDescriptorFactory.HUE_RED
                                else -> BitmapDescriptorFactory.HUE_GREEN
                            }

                            Log.d("MapActivity", "Adding marker: $name at $latLng")

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(name)
                                    .snippet(place.formattedAddress)
                                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            )
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("MapActivity", "Error fetching $oldType: $errorBody")
                        Toast.makeText(
                            this@MapActivity,
                            "Error fetching $oldType places",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<NearbySearchResponse>, t: Throwable) {
                    Log.e("MapActivity", "API call failed for $oldType", t)
                    Toast.makeText(
                        this@MapActivity,
                        "Failed to fetch $oldType: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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