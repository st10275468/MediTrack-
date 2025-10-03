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
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

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
            val start = etStart.text.toString()
            val end = etEnd.text.toString()
            val times = mutableListOf<String>()
            for (i in 0 until containerTimes.childCount) {
                val tv = containerTimes.getChildAt(i) as? TextView
                tv?.text?.toString()?.let { times.add(it) }
            }
            dismiss()
        }
    }
}
