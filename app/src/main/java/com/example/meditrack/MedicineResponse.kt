package com.example.meditrack

data class MedicineResponse(
    val results: List<Medicine>?
)

data class Medicine(
    val openfda: Openfda?,
    val purpose: List<String>?,
    val warnings: List<String>?,
    val dosage_and_administration: List<String>?
)

data class Openfda(
    val brand_name: List<String>?,
    val generic_name: List<String>?,
    val product_type: List<String>?
)
