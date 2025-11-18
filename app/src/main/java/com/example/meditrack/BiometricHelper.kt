package com.example.meditrack

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
/**
 * BiometricHelper.kt
 *
 * This activity handles the biometric functionality and prompt
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */

object BiometricHelper {

    //Method that checks if the device is able to use biometrics
    fun canAuthenticate(context: Context): Boolean{
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    }

    //Displays the biometric prompt to the user
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
                //Called when biometric authentication succeeds
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                //Called when biometric authentication gives an error
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }
                //Called when biometric authentication fails
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