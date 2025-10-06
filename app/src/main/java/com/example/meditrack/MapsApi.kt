package com.example.meditrack

import NearbySearchRequest
import NearbySearchResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MapsApi {
    @POST("v1/places:searchNearby")
    fun searchNearbyPlaces(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "places.displayName,places.location,places.types,places.formattedAddress",
        @Body request: NearbySearchRequest
    ): Call<NearbySearchResponse>
}