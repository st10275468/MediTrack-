package com.example.meditrack

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

/**
 * FireStoreHelper.kt
 *
 * Provides a function to save a user's email, optional name, and account creation timestamp.
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
object FireStoreHelper {

    // Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Saves user data to Firestore users collection
    fun saveUserToFirestore(context: Context, uid: String, email: String, name: String? = null) {
        val userMap = hashMapOf(
            "email" to email,
            "name" to name,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(context, "User saved in Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}