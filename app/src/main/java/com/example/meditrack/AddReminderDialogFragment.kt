import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.meditrack.LocaleHelper
import com.example.meditrack.R
import com.example.meditrack.Reminder
import com.example.meditrack.ReminderActivity
import com.example.meditrack.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.flexbox.FlexboxLayout

/**
 * AddReminderDialogFragment.kt
 *
 * This activity represents the dialog fragment that shows up when creating a new reminder
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */
class AddReminderDialogFragment : DialogFragment() {

    override fun onAttach(context: Context){
        super.onAttach(LocaleHelper.applyLocale(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Makes background transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_add_reminder, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            // Makes fullscreen
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setDimAmount(0.6f)
        dialog?.setCanceledOnTouchOutside(true)

        // Animation
        val card = dialog?.findViewById<View>(R.id.card_root)
        card?.alpha = 0f
        card?.scaleX = 0.9f
        card?.scaleY = 0.9f
        card?.animate()?.alpha(1f)?.scaleX(1f)?.scaleY(1f)?.setDuration(220)?.start()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Input fields and buttons for fragment
        val etStart = view.findViewById<EditText>(R.id.et_start_date)
        val etEnd = view.findViewById<EditText>(R.id.et_end_date)
        val btnAddTime = view.findViewById<Button>(R.id.btn_add_time)
        val containerTimes = view.findViewById<FlexboxLayout>(R.id.container_times)
        val spinnerMedicine = view.findViewById<Spinner>(R.id.spinner_medicine)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        // Retrieves current user
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Retrieves users medicines
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.uid)
                .collection("medicines")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val medicineList = mutableListOf<String>()
                    // Adds medicine to dropdown
                    for (doc in querySnapshot.documents) {
                        val medName = doc.getString("name")
                        medName?.let { medicineList.add(it) }
                    }

                    if (medicineList.isEmpty()) {
                        medicineList.add("No medicines added")
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        medicineList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerMedicine.adapter = adapter
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Medicines Failed to Load: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            spinnerMedicine.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Login to see medicines")
            )
        }

        // Displays date picker and handles result
        fun showDatePicker(target: EditText) {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val fmt = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    target.setText(fmt.format(cal.time))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Display time picker and handles result
        fun showTimePicker(onPicked: (String, String) -> Unit) {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), { _, h, m ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                }

                val fmt24 = SimpleDateFormat("HH:mm", Locale.getDefault())
                val fmt12 = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val timeForStorage = fmt24.format(cal.time)
                val timeForDisplay = fmt12.format(cal.time)

                onPicked(timeForDisplay, timeForStorage)

            }, hour, minute, false).show()
        }


        etStart.setOnClickListener { showDatePicker(etStart) }
        etEnd.setOnClickListener { showDatePicker(etEnd) }

        // Button to add new time
        btnAddTime.setOnClickListener {
            showTimePicker { displayTime, storageTime ->
                val chip = TextView(requireContext()).apply {
                    text = displayTime
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    setPadding(32, 16, 32, 16)
                    setBackgroundResource(R.drawable.bg_time_chip)

                    val params = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(24, 8, 24, 8)
                    layoutParams = params

                    tag = storageTime
                }
                containerTimes.addView(chip)
            }
        }



        // Button to cancel reminder adding
        btnCancel.setOnClickListener { dismiss() }

        // Button to save reminder
        btnSave.setOnClickListener {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrieve user inputs
            val spinnerMedicine = view.findViewById<Spinner>(R.id.spinner_medicine)
            val etDosage = view.findViewById<EditText>(R.id.et_dosage)
            val rgFrequency = view.findViewById<RadioGroup>(R.id.rg_frequency)
            val medicine = spinnerMedicine.selectedItem?.toString() ?: ""
            val start = etStart.text.toString()
            val end = etEnd.text.toString()
            val dosage = etDosage.text.toString()

            // Retrieve all times
            val times = mutableListOf<String>()
            for (i in 0 until containerTimes.childCount) {
                val tv = containerTimes.getChildAt(i) as? TextView
                tv?.tag?.toString()?.let { times.add(it) }
            }

            // Retrieve frequency of reminder
            val frequency = when (rgFrequency.checkedRadioButtonId) {
                R.id.rb_daily -> "Daily"
                R.id.rb_weekly -> "Weekly"
                R.id.rb_once -> "Once Off"
                else -> ""
            }

            // Input validation
            if (!validateInput(medicine, start, dosage, times, frequency)) return@setOnClickListener

            // Object to save in Firestore
            val reminderMap = hashMapOf(
                "medicine" to medicine,
                "startDate" to start,
                "endDate" to end,
                "dosage" to dosage,
                "times" to times,
                "frequency" to frequency,
                "createdAt" to System.currentTimeMillis()
            )

            // Save reminder to Firestore - reminders collection
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.uid)
                .collection("reminders")
                .add(reminderMap)
                .addOnSuccessListener { documentReference ->
                    // creates reminder to save for scheduling
                    val reminder = Reminder(
                        medicine = medicine,
                        dosage = dosage,
                        startDate = start,
                        endDate = end,
                        times = times,
                        frequency = frequency
                    )

                    // Schedules reminder
                    ReminderScheduler.scheduleReminder(
                        requireContext(),
                        reminder,
                        documentReference.id
                    )

                    Toast.makeText(requireContext(), "Reminder saved!", Toast.LENGTH_SHORT).show()
                    val reminderActivity = activity as? ReminderActivity
                    // Refresh reminders page on success
                    reminderActivity?.fetchReminders()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }

    /**
     * Method to validate all user input fields
     */
    private fun validateInput(
        medicine: String,
        start: String,
        dosage: String,
        times: List<String>,
        frequency: String
    ): Boolean {
        if (medicine.isEmpty() || medicine == "No medicines added" || medicine == "Login to see medicines") {
            Toast.makeText(requireContext(), "Please select a medicine", Toast.LENGTH_SHORT).show()
            return false
        }

        if (start.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a start date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (dosage.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a dosage", Toast.LENGTH_SHORT).show()
            return false
        }

        if (times.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one time", Toast.LENGTH_SHORT).show()
            return false
        }

        if (frequency.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a frequency", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
