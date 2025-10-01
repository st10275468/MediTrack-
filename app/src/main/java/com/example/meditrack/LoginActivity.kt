package com.example.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
            btnLogin.setOnClickListener{
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }

        val txtRegister = findViewById<TextView>(R.id.txtRegister)
            txtRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

    }
}