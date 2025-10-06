package com.example.meditrack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MedicineDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_detail)

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvDosage = findViewById<TextView>(R.id.tvDosage)
        val tvPurpose = findViewById<TextView>(R.id.tvPurpose)
        val tvWarnings = findViewById<TextView>(R.id.tvWarnings)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSaveFirebase)

        tvName.text = intent.getStringExtra("medicine_name") ?: "N/A"
        tvDosage.text = (intent.getStringExtra("dosage") ?: "N/A")
        tvPurpose.text = (intent.getStringExtra("purpose") ?: "N/A")
        tvWarnings.text = (intent.getStringExtra("warnings") ?: "N/A")

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medicineMap = hashMapOf(
                "name" to tvName.text.toString(),
                "savedAt" to System.currentTimeMillis(),
                "userId" to user.uid
            )

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
}
