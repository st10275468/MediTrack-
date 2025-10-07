package com.example.meditrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * MedicineAdapter.kt
 *
 * Class used for displaying medication lists in recyclerview
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class MedicineAdapter(
    private var medicines: List<Medicine>,
    private val onClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    // Represents single medicine
    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtMedicineName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.txtName.text = medicine.openfda?.brand_name?.getOrNull(0) ?: "Unknown"
        holder.itemView.setOnClickListener { onClick(medicine) }
    }

    // Return total items
    override fun getItemCount(): Int = medicines.size

    // Update and refreshes
    fun updateMedicines(newList: List<Medicine>) {
        medicines = newList
        notifyDataSetChanged()
    }
}
