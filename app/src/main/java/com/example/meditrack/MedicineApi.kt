package com.example.meditrack

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface to interact with FDA API
interface MedicineApi {
    @GET("drug/label.json")
    fun searchMedicine(
        @Query("search") search: String,
        @Query("limit") limit: Int = 10
    ): Call<MedicineResponse>

    @GET("drug/label.json")
    fun searchMedicineNDC(
        @Query("search") ndc: String,
        @Query("limit") limit: Int = 1
    ): Call<MedicineResponse>
}
