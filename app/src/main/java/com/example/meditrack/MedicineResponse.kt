package com.example.meditrack

/**
 * MedicineResponse.kt
 *
 * Data models to parse response from FDA API
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
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
