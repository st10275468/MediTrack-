package com.example.meditrack

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor


object BiometricHelper {

    fun canAuthenticate(context: Context): Boolean{
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    }

    fun showBiometricPrompt(
        context: Context,
        title: String = "Login",
        subtitle: String = "Authenticate to continue.",
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ){
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            context as androidx.fragment.app.FragmentActivity,
            executor,

            object: BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle).setNegativeButtonText("Cancel").build()

        prompt.authenticate(promptInfo)
    }


}