import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.Reminder

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onDeleteClick: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMedicine: TextView = itemView.findViewById(R.id.tvMedicine)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
        val tvTimes: TextView = itemView.findViewById(R.id.tvTimes)
        val tvNextDose: TextView = itemView.findViewById(R.id.tvNextDose)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.tvMedicine.text = reminder.medicine
        holder.tvDosage.text = "Dosage: ${reminder.dosage} | ${reminder.frequency}"
        holder.tvTimes.text = "Times: ${reminder.times.joinToString(" | ")}"
        holder.tvNextDose.text = "Next Dose: ${reminder.times.firstOrNull() ?: ""}"
        holder.btnDelete.setOnClickListener {
            onDeleteClick(reminder)
        }
    }

    override fun getItemCount(): Int = reminders.size
}
