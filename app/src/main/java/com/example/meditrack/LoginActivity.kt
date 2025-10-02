package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        btnLogin.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            loginUser(email, password)
        }

        txtRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loginUser(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Login succesful", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }

        }
    }
}