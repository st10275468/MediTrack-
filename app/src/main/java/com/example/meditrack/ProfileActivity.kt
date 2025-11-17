package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

/**
 * ProfileActivity.kt
 *
 * This activity allows users to view and edit their own personal medical profile.
 * Saves all data to firestore.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Retrieves logged in user
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Setup for top tab menu
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

        //Settings Menu functionality
        val settingsIcon = findViewById<ImageView>(R.id.imageView4)
        settingsIcon.setOnClickListener {
            val popup = PopupMenu(this, settingsIcon)
            popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_language -> {
                        Toast.makeText(this, "Feature not implemented yet", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Input fields
        val fullNameInput = findViewById<EditText>(R.id.etFullName)
        val ageInput = findViewById<EditText>(R.id.etAge)
        val bloodTypeInput = findViewById<EditText>(R.id.etBloodType)
        val allergiesInput = findViewById<EditText>(R.id.etAllergies)
        val chronicConditionsInput = findViewById<EditText>(R.id.etChronicConditions)
        val medInput1 = findViewById<EditText>(R.id.etMed1)
        val medInput2 = findViewById<EditText>(R.id.etMed2)
        val medInput3 = findViewById<EditText>(R.id.etMed3)
        val medInput4 = findViewById<EditText>(R.id.etMed4)
        val notesInput = findViewById<EditText>(R.id.etNotes)

        // Save buttons
        val btnSavePersonalDetails = findViewById<Button>(R.id.btnSavePersonalDetails)
        val btnSaveMedicalDetails = findViewById<Button>(R.id.btnSaveMedicalDetails)
        val btnSaveMedicationDetails = findViewById<Button>(R.id.btnSaveMedicationDetails)
        val btnSaveNotesDetails = findViewById<Button>(R.id.btnSaveNoteDetails)

        fun loadMedicalProfileData(){
            if (user == null) return
            val userProfileRef = db.collection("users").document(user.uid).collection("medicalProfile")

            // Pre-fills personal detail hints with Firestore data if available
            userProfileRef.document("personalDetails").get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        doc.getString("Name")?.takeIf { it.isNotBlank() }?.let { fullNameInput.hint = it }
                        doc.getString("Age")?.takeIf { it.isNotBlank() }?.let { ageInput.hint = it }
                        val genderGroup = findViewById<RadioGroup>(R.id.rbGender)
                        when (doc.getString("Gender")) {
                            "Male" -> genderGroup.check(R.id.rbMale)
                            "Female" -> genderGroup.check(R.id.rbFemale)
                        }
                    }
                }

            // Pre-fills medical detail hints with Firestore data if available
            userProfileRef.document("medicalDetails").get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        doc.getString("bloodType")?.takeIf { it.isNotBlank() }?.let { bloodTypeInput.hint = it }
                        doc.getString("allergies")?.takeIf { it.isNotBlank() }?.let { allergiesInput.hint = it }
                        doc.getString("chronicConditions")?.takeIf { it.isNotBlank() }?.let { chronicConditionsInput.hint = it }
                    }
                }

            // Pre-fills medication detail hints with Firestore data if available
            userProfileRef.document("medicationDetails").get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        doc.getString("medication1")?.takeIf { it.isNotBlank() }?.let { medInput1.hint = it }
                        doc.getString("medication2")?.takeIf { it.isNotBlank() }?.let { medInput2.hint = it }
                        doc.getString("medication3")?.takeIf { it.isNotBlank() }?.let { medInput3.hint = it }
                        doc.getString("medication4")?.takeIf { it.isNotBlank() }?.let { medInput4.hint = it }
                    }
                }

            // Pre-fills notes hint with Firestore data if available
            userProfileRef.document("notesDetails").get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        doc.getString("notes")?.takeIf { it.isNotBlank() }?.let { notesInput.hint = it }
                    }
                }
        }

        // Loads medical profile initially
        loadMedicalProfileData()

        // Saves personal details to firestore
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
                    fullNameInput.text.clear()
                    ageInput.text.clear()
                    loadMedicalProfileData()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Saves medical details to firestore
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
                    bloodTypeInput.text.clear()
                    allergiesInput.text.clear()
                    chronicConditionsInput.text.clear()
                    loadMedicalProfileData()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Saves Medication details to firestore
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
                    medInput1.text.clear()
                    medInput2.text.clear()
                    medInput3.text.clear()
                    medInput4.text.clear()
                    loadMedicalProfileData()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving medications: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Saves note details to firestore
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
                    notesInput.text.clear()
                    loadMedicalProfileData()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving notes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}