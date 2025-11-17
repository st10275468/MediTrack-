package com.example.meditrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * RegisterActivity.kt
 *
 * This activity allows users to register and save their user to FirebaseAuth and Firestore
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance()

        // Input fields
        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.password)
        val passwordConfirmInput = findViewById<EditText>(R.id.confirmPassword)

        // Register button
        val registerBtn = findViewById<Button>(R.id.btnRegister)

        // Sets parameters and calls registerUser method
        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = passwordConfirmInput.text.toString().trim()

            registerUser(email, password, confirmPassword)
        }
    }

    /**
     * Methods to register user
     * Validates input fields, creates FirebaseAuth user then saves user to Firestore
     */
    private fun registerUser(email: String, password: String, confirmPassword: String){
        // Check for empty input fields
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Checks if passwords match
        if (password != confirmPassword){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        //Creates user with email and password in FirebaseAuth
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                val user = auth.currentUser

                //Saves user to Firestore
                FireStoreHelper.saveUserToFirestore(this,user!!.uid, user.email!!)

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}