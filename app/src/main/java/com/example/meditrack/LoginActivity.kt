package com.example.meditrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * LoginActivity.kt
 *
 * This activity allows users to login using FirebaseAuth email and password, and Google sign in options
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup for Google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Input fields and buttons
        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.password)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSSOLogin = findViewById<Button>(R.id.btnLoginSSO)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Sets parameters and calls loginUser method for email and password login
        btnLogin.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            loginUser(email, password)
        }

        // Google sign in
        btnSSOLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // Redirects to registration screen
        txtRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

        //Retrieving the saved userID
        val savedUID = SecureStorage.getUID(this)

        //Checking if the userID is not null and if the device can use biometric authentication
        if(savedUID != null && SecureStorage.biometricsEnabled(this)){
            if(BiometricHelper.canAuthenticate(this)){

                //Displaying the fingerprint dialog
                BiometricHelper.showBiometricPrompt(
                    context = this,
                    title = "Login With Biometrics",
                    subtitle = "Authenticate to continue",
                    onSuccess = {
                        Toast.makeText(this, "Biometric Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                            onFailure = {
                                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
                            }
                        )


            }
        }

    }

    /**
     * Method for email and password login using FirebaseAuth
     */
    private fun loginUser(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                val uid = auth.currentUser?.uid
                uid?.let { SecureStorage.saveUID(this, it) }

                if(BiometricHelper.canAuthenticate(this)){
                    SecureStorage.enableBiometrics(this)
                }

                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * Method to handle the result of google sign in
     */
    private val googleSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Method to login using FirebaseAuth Google sign-in
     */
    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val uid = user?.uid
                uid?.let { SecureStorage.saveUID(this, it) }

                if(BiometricHelper.canAuthenticate(this)){
                    SecureStorage.enableBiometrics(this)
                }

                Toast.makeText(this, "Signed in with Google", Toast.LENGTH_SHORT).show()

                // Save user to Firestore
                user?.let {
                    FireStoreHelper.saveUserToFirestore(
                        this,
                        it.uid,
                        it.email ?: "",
                        it.displayName
                    )
                }

                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Firebase auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}