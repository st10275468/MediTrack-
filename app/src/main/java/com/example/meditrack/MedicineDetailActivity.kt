package com.example.meditrack

import RetrofitInstance
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

/**
 * MedicineDetailActivity.kt
 *
 * Activity to display info for specicic selected medicine
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */

class MedicineDetailActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvDosage: TextView
    private lateinit var tvPurpose: TextView
    private lateinit var tvWarnings: TextView
    private lateinit var btnBack: Button
    private lateinit var btnSave: Button

    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_detail)

        // UI components
        tvName = findViewById(R.id.tvName)
        tvDosage = findViewById(R.id.tvDosage)
        tvPurpose = findViewById(R.id.tvPurpose)
        tvWarnings = findViewById(R.id.tvWarnings)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSaveFirebase)

        //Retrieving medication data if barcode is scanned
        val scannedBarcode = intent.getStringExtra("BARCODE")
        if (!scannedBarcode.isNullOrEmpty()){
            fetchMedicineByBarcode(scannedBarcode)
        }
        else{
            //Setting fields to N/A if fields are not found
            tvName.text = intent.getStringExtra("medicine_name") ?: "N/A"
            tvDosage.text = (intent.getStringExtra("dosage") ?: "N/A")
            tvPurpose.text = (intent.getStringExtra("purpose") ?: "N/A")
            tvWarnings.text = (intent.getStringExtra("warnings") ?: "N/A")

        }

        // Back button
        btnBack.setOnClickListener { finish() }

        // Save button
        btnSave.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare medicine for Firestore
            val medicineMap = hashMapOf(
                "name" to tvName.text.toString(),
                "savedAt" to System.currentTimeMillis(),
                "userId" to user.uid
            )

            // Save data to user document in Firestore
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.uid)
                .collection("medicines")
                .add(medicineMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Medicine saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save medicine: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //Method uses barcode number to retrieve medications that match from the API
    private fun fetchMedicineByBarcode(barcode: String){

        //Calling a method to change the barcode number into the correct format
        val ndcCode = convertBarcodeToNDC(barcode)
        val query = "openfda.product_ndc:$ndcCode"

        //Api request
        RetrofitInstance.api.searchMedicineNDC(query).enqueue(object : retrofit2.Callback<MedicineResponse> {
            override fun onResponse(call : retrofit2.Call<MedicineResponse>, response: retrofit2.Response<MedicineResponse>){
                if (response.isSuccessful){

                    val medicine = response.body()?.results?.firstOrNull()

                    if(medicine != null){
                        //Method to clear fields
                        fun clean(text: List<String>?): String {
                            return text?.joinToString("\n")
                                ?.replace("Purpose", "", ignoreCase = true)
                                ?.replace("Dosage", "", ignoreCase = true)
                                ?.replace("Warnings", "", ignoreCase = true)
                                ?.trim()
                                ?: "N/A"
                        }
                        //Populating the fields with the medication data retrieved
                        tvName.text = medicine.openfda?.brand_name?.getOrNull(0) ?: "N/A"
                        tvDosage.text = clean(medicine.dosage_and_administration)
                        tvPurpose.text = clean(medicine.purpose)
                        tvWarnings.text = clean(medicine.warnings)
                    }
                    else{
                        //If no medication is found
                        tvName.text = "Medicine not found"
                        tvDosage.text = "N/A"
                        tvPurpose.text = "N/A"
                        tvWarnings.text = "N/A"
                    }
                }
                else{
                    Toast.makeText(this@MedicineDetailActivity, "Failed to retrieve medicine info: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: retrofit2.Call<MedicineResponse>, t: Throwable){
                Toast.makeText(this@MedicineDetailActivity, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //Method to convert the barcode number into the correct format
    fun convertBarcodeToNDC(barcode: String): String{
        if(barcode.length != 12){
            return barcode
        }

        val withoutCheck = barcode.substring(0, 11)

        return when (withoutCheck.length){
            10 -> {
                val labeler = withoutCheck.take(4)
                val product = withoutCheck.substring(4,8)
                val packageCode = withoutCheck.takeLast(2)
                "$labeler-$product-$packageCode"
            }

        11 -> {
            val labeler = withoutCheck.take(5)
            val product = withoutCheck.substring(5,9)
            val packageCode = withoutCheck.takeLast(2)
            "$labeler-$product-$packageCode"
        }
            else -> barcode

        }
    }
}
