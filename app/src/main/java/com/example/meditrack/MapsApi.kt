package com.example.meditrack

import GeoapifyPlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApi {
    @GET("places")
    fun getNearbyPlaces(
        @Query("categories") categories: String,
        @Query("filter") filter: String,
        @Query("apiKey") apiKey: String
    ): Call<GeoapifyPlacesResponse>
}