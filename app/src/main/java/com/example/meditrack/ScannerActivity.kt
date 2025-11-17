package com.example.meditrack

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ScannerActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
    }
}