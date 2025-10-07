import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ReminderAdapter.kt
 *
 * Adapter to display reminders in recyclerview
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onDeleteClick: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    /**
     * View holder to reference specific components for each reminder
     */
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
        holder.tvTimes.text = "Times: ${reminder.times.joinToString(" | ") { time24 ->
            try {
                val date = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time24)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                time24
            }
        }}"
        holder.tvNextDose.text = "Next Dose: ${getNextDoseDisplay(reminder)}"
        holder.btnDelete.setOnClickListener {
            onDeleteClick(reminder)
        }
    }

    /**
     * Method to calculate next dose datetime
     */
    private fun getNextDoseDisplay(reminder: Reminder): String {
        val nextTime = reminder.times.firstOrNull() ?: return "No dose"
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeDate = try { sdfTime.parse(nextTime) } catch (e: Exception) { Date() }
        val startDate = try {
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(reminder.startDate ?: "")
        } catch (e: Exception) { Date() }

        val nextDoseCal = Calendar.getInstance().apply {
            time = startDate ?: Date()
            val calTime = Calendar.getInstance().apply { time = timeDate ?: Date() }
            set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()

        when (reminder.frequency.lowercase()) {
            "daily" -> while (nextDoseCal.before(now)) nextDoseCal.add(Calendar.DATE, 1)
            "weekly" -> while (nextDoseCal.before(now)) nextDoseCal.add(Calendar.WEEK_OF_YEAR, 1)
            "once" -> {}
        }

        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(nextDoseCal.time)
        val formattedTime = sdfDisplay.format(nextDoseCal.time)
        return "$formattedTime | $dayOfWeek"
    }


    override fun getItemCount(): Int = reminders.size
}
