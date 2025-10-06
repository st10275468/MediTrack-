import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.meditrack.R
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddReminderDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_add_reminder, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setDimAmount(0.6f)
        dialog?.setCanceledOnTouchOutside(true)

        val card = dialog?.findViewById<View>(R.id.card_root)
        card?.alpha = 0f
        card?.scaleX = 0.9f
        card?.scaleY = 0.9f
        card?.animate()?.alpha(1f)?.scaleX(1f)?.scaleY(1f)?.setDuration(220)?.start()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etStart = view.findViewById<EditText>(R.id.et_start_date)
        val etEnd = view.findViewById<EditText>(R.id.et_end_date)
        val btnAddTime = view.findViewById<Button>(R.id.btn_add_time)
        val containerTimes = view.findViewById<FlexboxLayout>(R.id.container_times)
        val spinnerMedicine = view.findViewById<Spinner>(R.id.spinner_medicine)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.uid)
                .collection("medicines")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val medicineList = mutableListOf<String>()
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

        fun showTimePicker(onPicked: (String) -> Unit) {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            TimePickerDialog(requireContext(), { _, h, m ->
                val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
                val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                onPicked(fmt.format(cal.time))
            }, hour, minute, false).show()
        }

        etStart.setOnClickListener { showDatePicker(etStart) }
        etEnd.setOnClickListener { showDatePicker(etEnd) }

        btnAddTime.setOnClickListener {
            showTimePicker { timeString ->
                val chip = TextView(requireContext()).apply {
                    text = timeString
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
                }
                containerTimes.addView(chip)
            }
        }


        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "You must be logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val spinnerMedicine = view.findViewById<Spinner>(R.id.spinner_medicine)
            val etDosage = view.findViewById<EditText>(R.id.et_dosage)
            val rgFrequency = view.findViewById<RadioGroup>(R.id.rg_frequency)

            val medicine = spinnerMedicine.selectedItem?.toString() ?: ""
            val start = etStart.text.toString()
            val end = etEnd.text.toString()
            val dosage = etDosage.text.toString()

            val times = mutableListOf<String>()
            for (i in 0 until containerTimes.childCount) {
                val tv = containerTimes.getChildAt(i) as? TextView
                tv?.text?.toString()?.let { times.add(it) }
            }

            val frequency = when (rgFrequency.checkedRadioButtonId) {
                R.id.rb_daily -> "Daily"
                R.id.rb_weekly -> "Weekly"
                R.id.rb_once -> "Once Off"
                else -> ""
            }

            val reminderMap = hashMapOf(
                "medicine" to medicine,
                "startDate" to start,
                "endDate" to end,
                "dosage" to dosage,
                "times" to times,
                "frequency" to frequency,
                "createdAt" to System.currentTimeMillis()
            )

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users")
                .document(user.uid)
                .collection("reminders")
                .add(reminderMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reminder saved!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
