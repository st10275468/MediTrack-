package com.example.meditrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import retrofit2.Call

/**
 * SearchActivity.kt
 *
 * This activity allows users to search for medications using FDA API
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: MedicineAdapter

    override fun attachBaseContext(newBase: Context){
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //Tab Menu functionality
        val tabMenu = findViewById<TabLayout>(R.id.TabMenu)
        tabMenu.getTabAt(1)?.select()
        tabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {val intent = Intent(this@SearchActivity, DashboardActivity::class.java)
                        startActivity(intent)}
                    1 -> {}
                    2 -> {
                        val intent = Intent(this@SearchActivity, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(this@SearchActivity, ReminderActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        //Settings Menu functionality
        val settingsIcon = findViewById<ImageView>(R.id.imageView4)
        settingsIcon.setOnClickListener {
            val popup = PopupMenu(this, settingsIcon)
            popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_language -> {
                        val languagePopup = PopupMenu(this, settingsIcon)
                        languagePopup.menu.add("English")
                        languagePopup.menu.add("Afrikaans")

                        languagePopup.setOnMenuItemClickListener { langItem ->
                            val code = if (langItem.title == "English") "en" else "af"
                            LocaleHelper.setLocale(this, code)
                            LocaleHelper.refreshActivity(this)
                            true
                        }
                        languagePopup.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Setup recycler and search views
        val rvMedicines = findViewById<RecyclerView>(R.id.rvMedicines)
        val svSearch = findViewById<androidx.appcompat.widget.SearchView>(R.id.svSearch)
        svSearch.isIconified = false
        svSearch.isFocusable = true
        svSearch.isFocusableInTouchMode = true

        // Initialize medicine adapter
        adapter = MedicineAdapter(listOf()) { medicine ->
            val intent = Intent(this, MedicineDetailActivity::class.java)

            // format text
            fun clean(text: String?): String {
                return text
                    ?.replace("Purpose", "", ignoreCase = true)
                    ?.replace("Dosage", "", ignoreCase = true)
                    ?.replace("Warnings", "", ignoreCase = true)
                    ?.trim()
                    ?: "N/A"
            }

            // passes data to medicine details activity
            intent.putExtra("medicine_name", medicine.openfda?.brand_name?.getOrNull(0))
            intent.putExtra("purpose", clean(medicine.purpose?.joinToString("\n")))
            intent.putExtra("warnings", clean(medicine.warnings?.joinToString("\n")))
            intent.putExtra("dosage", clean(medicine.dosage_and_administration?.joinToString("\n")))

            startActivity(intent)
        }

        rvMedicines.adapter = adapter
        rvMedicines.layoutManager = LinearLayoutManager(this)

        svSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMedicine(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isNotEmpty()) {
                        searchMedicine(it)
                    } else {
                        adapter.updateMedicines(emptyList())
                    }
                }
                return true
            }
        })


    }

    /**
     * Uses user input to search for medication
     */
    private fun searchMedicine(userInput: String) {
         val query = "openfda.brand_name:$userInput*"

        // Make API call
         RetrofitInstance.api.searchMedicine(query).enqueue(object : retrofit2.Callback<MedicineResponse> {
            override fun onResponse(call: retrofit2.Call<MedicineResponse>, response: retrofit2.Response<MedicineResponse>) {
                if (response.isSuccessful) {
                    val medicines = response.body()?.results ?: emptyList()

                    // Filter
                    val filteredMedicines = medicines.filter { med ->
                        val types = med.openfda?.product_type ?: emptyList()
                        val isDrug = types.any { it.contains("DRUG", ignoreCase = true) }
                        val hasBrand = !med.openfda?.brand_name.isNullOrEmpty()
                        isDrug && hasBrand
                    }

                    adapter.updateMedicines(filteredMedicines)
                }
            }

            override fun onFailure(call: Call<MedicineResponse>, t: Throwable) {
                Toast.makeText(this@SearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}