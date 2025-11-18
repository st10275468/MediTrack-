package com.example.meditrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

/**
 * ScannerActivity.kt
 *
 * This activity opens the barcode scanner and redirects the user to the medicineDetailActivity
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */

class ScannerActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Starting the barcode scanner as soon as the activity is opened
        startBarcodeScanner()
    }

    //Starts the ZXing barcode scanner
    private fun startBarcodeScanner(){
        val integrator = IntentIntegrator(this)

        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan medication barcode")
        integrator.setBeepEnabled(true)
        integrator.setCameraId(0)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()

    }

    //Handles the retrieved data from the scanner
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){
            if(result.contents == null){
                finish()
            }
            else{
                val barcodeValue = result.contents

                //Passes the data and opens the medicineDetailActivity
                val intent = Intent(this, MedicineDetailActivity::class.java)
                intent.putExtra("BARCODE", barcodeValue)
                startActivity(intent)
                finish()
            }
        }

    }
}