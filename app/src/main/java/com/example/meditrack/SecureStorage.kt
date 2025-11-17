package com.example.meditrack

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {

    private const val pref_name = "secure_prefs"
    private const val key_uid = "firebase_uid"
    private const val key_biometric_enabled = "biometric_enabled"

    private fun getPrefs(context: Context) : android.content.SharedPreferences{
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        return EncryptedSharedPreferences.create(
            context,
            pref_name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    fun saveUID(context: Context, uid: String){
        getPrefs(context).edit().putString(key_uid, uid).apply()
    }

    fun getUID(context: Context): String?{
        return getPrefs(context).getString(key_uid, null)
    }

    fun enableBiometrics(context: Context){
        getPrefs(context).edit().putBoolean(key_biometric_enabled, true).apply()
    }

    fun biometricsEnabled(context: Context): Boolean{
        return getPrefs(context).getBoolean(key_biometric_enabled, false)
    }
}
