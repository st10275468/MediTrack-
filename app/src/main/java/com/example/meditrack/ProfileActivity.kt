package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.tabs.TabLayout

class ProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val tabMenu = findViewById<TabLayout>(R.id.TabMenu)
        tabMenu.getTabAt(2)?.select()
        tabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {val intent = Intent(this@ProfileActivity, DashboardActivity::class.java)
                        startActivity(intent)}
                    1 -> { val intent = Intent(this@ProfileActivity, SearchActivity::class.java)
                        startActivity(intent)}
                    2 -> {

                    }
                    3 -> { val intent = Intent(this@ProfileActivity, ReminderActivity::class.java)
                        startActivity(intent)}
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val fullNameInput = findViewById<EditText>(R.id.etFullName)
        val ageInput = findViewById<EditText>(R.id.etAge)
        val btnSavePersonalDetails = findViewById<Button>(R.id.btnSavePersonalDetails)
        val bloodTypeInput = findViewById<EditText>(R.id.etBloodType)
        val allergiesInput = findViewById<EditText>(R.id.etAllergies)
        val chronicConditionsInput = findViewById<EditText>(R.id.etChronicConditions)
        val btnSaveMedicalDetails = findViewById<Button>(R.id.btnSaveMedicalDetails)
        val medInput1 = findViewById<EditText>(R.id.etMed1)
        val medInput2 = findViewById<EditText>(R.id.etMed2)
        val medInput3 = findViewById<EditText>(R.id.etMed3)
        val medInput4 = findViewById<EditText>(R.id.etMed4)
        val btnSaveMedicationDetails = findViewById<Button>(R.id.btnSaveMedicationDetails)
        val notesInput = findViewById<EditText>(R.id.etNotes)
        val btnSaveNotesDetails = findViewById<Button>(R.id.btnSaveNoteDetails)

        btnSavePersonalDetails.setOnClickListener {
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullName = fullNameInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val genderGroup = findViewById<RadioGroup>(R.id.rbGender)
            val gender = when (genderGroup.checkedRadioButtonId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                else -> ""
            }

            val personalDetailsMap = hashMapOf(
                "Name" to fullName,
                "Age" to age,
                "Gender" to gender
            )

            db.collection("users")
                .document(user.uid)
                .collection("medicalProfile")
                .document("personalDetails")
                .set(personalDetailsMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Personal details saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnSaveMedicalDetails.setOnClickListener {
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bloodType = bloodTypeInput.text.toString().trim()
            val allergies = allergiesInput.text.toString().trim()
            val chronicConditions = chronicConditionsInput.text.toString().trim()

            val medicalDetailsMap = hashMapOf(
                "bloodType" to bloodType,
                "allergies" to allergies,
                "chronicConditions" to chronicConditions
            )

            db.collection("users")
                .document(user.uid)
                .collection("medicalProfile")
                .document("medicalDetails")
                .set(medicalDetailsMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Medical details saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnSaveMedicationDetails.setOnClickListener {
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val med1 = medInput1.text.toString().trim()
            val med2 = medInput2.text.toString().trim()
            val med3 = medInput3.text.toString().trim()
            val med4 = medInput4.text.toString().trim()

            val medicationDetailsMap = hashMapOf(
                "medication1" to med1,
                "medication2" to med2,
                "medication3" to med3,
                "medication4" to med4
            )

            db.collection("users")
                .document(user.uid)
                .collection("medicalProfile")
                .document("medicationDetails")
                .set(medicationDetailsMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Medication details saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving medications: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnSaveNotesDetails.setOnClickListener {
            if (user == null) {
                Toast.makeText(this, "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notes = notesInput.text.toString().trim()

            val notesDetailsMap = hashMapOf(
                "notes" to notes
            )

            db.collection("users")
                .document(user.uid)
                .collection("medicalProfile")
                .document("notesDetails")
                .set(notesDetailsMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Notes saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving notes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}