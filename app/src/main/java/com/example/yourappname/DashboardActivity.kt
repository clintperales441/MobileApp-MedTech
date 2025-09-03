package com.example.yourappname

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourappname.data.Medicine
import com.example.yourappname.data.MedicineData
import com.example.yourappname.helper.MedicineAdapter
import com.example.yourappname.helper.ProfileManager
import com.example.yourappname.helper.ThemeHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MedicineAdapter
    private val medicineList = mutableListOf<Medicine>()
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var emptyStateView: View

    private val todayMedicationsList = mutableListOf<Medicine>()
    private lateinit var todayMedicationAdapter: MedicineAdapter
    private lateinit var todayMedicationsRecyclerView: RecyclerView
    private lateinit var todayEmptyStateView: View

    private lateinit var formStepOne: LinearLayout
    private lateinit var btnProceed: Button
    private lateinit var nameInput: AutoCompleteTextView
    private lateinit var descInput: EditText
    private lateinit var dosageInput: EditText
    private lateinit var fab: FloatingActionButton

    private lateinit var navProfilePicture: ImageView

    private var tempName = ""
    private var tempDesc = ""
    private var tempDosage = ""

    private val REMINDER_ACTION_REQUEST_CODE = 100

    private lateinit var usernameTextView: TextView
    private lateinit var dateTextView: TextView

    private lateinit var daysContainer: LinearLayout
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton

    private val calendar = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    private var dayViews = mutableListOf<TextView>()
    private var dayBackgrounds = mutableListOf<FrameLayout>()

    private lateinit var themeHelper: ThemeHelper

    private lateinit var helloUserText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        navProfilePicture = findViewById(R.id.nav_profile_picture)
        ProfileManager.loadProfilePicture(this, navProfilePicture)

        helloUserText = findViewById(R.id.usernameTextView)

        sharedPrefs = getSharedPreferences("med_prefs", MODE_PRIVATE)
        loadMedicines()


        updateTodayMedications()

        themeHelper = ThemeHelper.attach(this)

        initializeTopBarAndCalendar()

        formStepOne = findViewById(R.id.medicineFormStepOne)
        btnProceed = findViewById(R.id.btnProceed)
        fab = findViewById(R.id.fabToggleForm)
        nameInput = findViewById(R.id.editTextMedicineName)
        descInput = findViewById(R.id.editTextMedicineDesc)
        dosageInput = findViewById(R.id.editTextMedicineDosage)

        recycler = findViewById(R.id.recyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = MedicineAdapter(
            medicineList,
            editAction = { position -> editMedicine(position) },
            removeAction = { position -> removeMedicine(position) },
            longPressAction = { position -> showOverlayButtons(position) }
        )

        recycler.adapter = adapter

        updateEmptyState()

        fab.setOnClickListener {
            formStepOne.visibility = View.VISIBLE
        }

        btnProceed.setOnClickListener {
            tempName = nameInput.text.toString()
            tempDesc = descInput.text.toString()
            tempDosage = dosageInput.text.toString()

            if (tempName.isEmpty() || tempDesc.isEmpty() || tempDosage.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val timeCalendar = Calendar.getInstance()
            val hour = timeCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = timeCalendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                timeCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                timeCalendar.set(Calendar.MINUTE, selectedMinute)
                timeCalendar.set(Calendar.SECOND, 0)
                timeCalendar.set(Calendar.MILLISECOND, 0)

                if (timeCalendar.timeInMillis <= System.currentTimeMillis()) {
                    timeCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val reminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                val newMedicine = Medicine(tempName, tempDesc, tempDosage, reminderTime)
                medicineList.add(newMedicine)
                adapter.notifyItemInserted(medicineList.size - 1)
                saveMedicines()

                updateEmptyState()

                setDailyReminder(timeCalendar, tempName)

                clearForm()
                Toast.makeText(this, "Reminder set for: $reminderTime", Toast.LENGTH_LONG).show()
            }, hour, minute, true)

            timePickerDialog.show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val suggestionAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, MedicineData.medicineNames)
        nameInput.setAdapter(suggestionAdapter)
        nameInput.threshold = 1

        val medicineName = intent.getStringExtra("medicineName")
        val medicinePosition = intent.getIntExtra("medicinePosition", -1)
        if (medicineName != null && medicinePosition >= 0) {
            openReminderAction(medicineName, medicinePosition)
        }
    }


    private fun updateTodayMedications() {
        todayMedicationsList.clear()

        todayMedicationsList.addAll(medicineList.sortedBy { it.reminderTime })

        if (::todayMedicationAdapter.isInitialized) {
            todayMedicationAdapter.notifyDataSetChanged()
        }

        updateTodayEmptyState()
    }

    private fun updateTodayEmptyState() {
        try {
            if (::todayEmptyStateView.isInitialized && ::todayMedicationsRecyclerView.isInitialized) {
                if (todayMedicationsList.isEmpty()) {
                    todayEmptyStateView.visibility = View.VISIBLE
                    todayMedicationsRecyclerView.visibility = View.GONE
                } else {
                    todayEmptyStateView.visibility = View.GONE
                    todayMedicationsRecyclerView.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
        }
    }


    private fun showDetailDialog(position: Int) {
        val medicine = medicineList[position]
        val dialog = AlertDialog.Builder(this)
            .setTitle(medicine.name)
            .setMessage("Description: ${medicine.description}\nDosage: ${medicine.dosage}\nReminder: ${medicine.reminderTime ?: "None"}")
            .setPositiveButton("Edit") { _, _ -> editMedicine(position) }
            .setNegativeButton("Close", null)
            .create()
        dialog.show()
    }

    private fun updateEmptyState() {
        try {
            if (::emptyStateView.isInitialized && ::recycler.isInitialized) {
                if (medicineList.isEmpty()) {
                    emptyStateView.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyStateView.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun initializeTopBarAndCalendar() {

        usernameTextView = findViewById(R.id.usernameTextView)
        val userName = sharedPrefs.getString("user_name", "User")
        usernameTextView.text = userName


        dateTextView = findViewById(R.id.dateTextView)
        daysContainer = findViewById(R.id.daysContainer)
        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)

        btnPrevWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateCalendarDays()
        }

        btnNextWeek.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateCalendarDays()
        }

        setupCalendarDays()
        updateCalendarDays()
    }

    private fun setupCalendarDays() {
        daysContainer.removeAllViews()
        dayViews.clear()
        dayBackgrounds.clear()

        for (i in 0 until 7) {
            val dayContainer = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val dayView = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                    width = resources.getDimensionPixelSize(R.dimen.day_circle_size)
                    height = resources.getDimensionPixelSize(R.dimen.day_circle_size)
                }
                gravity = Gravity.CENTER
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@DashboardActivity, android.R.color.black))

                setOnClickListener {
                    val dayOfWeek = i + 1
                    val clickedDate = Calendar.getInstance().apply {
                        time = calendar.time
                        set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    }

                    selectedDate = clickedDate
                    updateSelectedDayHighlight()
                    updateDateText()

                }
            }

            dayContainer.addView(dayView)
            daysContainer.addView(dayContainer)

            dayViews.add(dayView)
            dayBackgrounds.add(dayContainer)
        }
    }

    private fun updateCalendarDays() {
        val tempCalendar = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        for (i in 0 until 7) {
            val dayOfMonth = tempCalendar.get(Calendar.DAY_OF_MONTH)
            dayViews[i].text = dayOfMonth.toString()

            dayViews[i].tag = tempCalendar.timeInMillis

            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        updateSelectedDayHighlight()
        updateDateText()
    }

    private fun updateSelectedDayHighlight() {
        val today = Calendar.getInstance()
        val isSelectedWeek = isSameWeek(selectedDate, calendar)

        for (i in 0 until 7) {
            val dayView = dayViews[i]
            val dayContainer = dayBackgrounds[i]

            val dayTimeMillis = dayView.tag as Long
            val dayDate = Calendar.getInstance().apply { timeInMillis = dayTimeMillis }

            val isToday = isSameDay(dayDate, today)
            val isSelected = isSelectedWeek && isSameDay(dayDate, selectedDate)

            when {
                isSelected -> {
                    dayView.setBackgroundResource(R.drawable.circle_blue)
                    dayView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                }
                isToday -> {
                    dayView.setBackgroundResource(R.drawable.circle_outline)
                    dayView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                }
                else -> {
                    dayView.background = null
                    dayView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                }
            }

            val dayOfWeekIndex = (dayDate.get(Calendar.DAY_OF_WEEK) - 1) % 7
            val dayOfWeekId = when (dayOfWeekIndex) {
                0 -> R.id.day_sun
                1 -> R.id.day_mon
                2 -> R.id.day_tue
                3 -> R.id.day_wed
                4 -> R.id.day_thu
                5 -> R.id.day_fri
                else -> R.id.day_sat
            }

            val dayOfWeekTextView = findViewById<TextView>(dayOfWeekId)

            if (isSelected) {
                dayOfWeekTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            } else {
                dayOfWeekTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        val isToday = isSameDay(selectedDate, Calendar.getInstance())

        dateTextView.text = if (isToday) {
            "Today, $formattedDate"
        } else {
            formattedDate
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun saveMedicines() {
        val gson = Gson()
        val json = gson.toJson(medicineList)
        sharedPrefs.edit().putString("medicine_list", json).apply()

        updateTodayMedications()
    }

    private fun loadMedicines() {
        val gson = Gson()
        val json = sharedPrefs.getString("medicine_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Medicine>>() {}.type
            val savedList: List<Medicine> = gson.fromJson(json, type)
            medicineList.clear()
            medicineList.addAll(savedList)
        }
    }

    private fun clearForm() {
        nameInput.text.clear()
        descInput.text.clear()
        dosageInput.text.clear()
        tempName = ""
        tempDesc = ""
        tempDosage = ""
        formStepOne.visibility = View.GONE

        updateTodayMedications()
    }

    private fun removeMedicine(position: Int) {
        if (position in medicineList.indices) {
            val medicine = medicineList[position]

            val intent = Intent(this, ReminderReceiver::class.java)
            intent.putExtra("medicineName", medicine.name)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                medicine.name.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            medicineList.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveMedicines()

            updateEmptyState()
        }
    }

    private fun editMedicine(position: Int) {
        val medicine = medicineList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_medicine, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.editName)
        val descEdit = dialogView.findViewById<EditText>(R.id.editDescription)
        val dosageEdit = dialogView.findViewById<EditText>(R.id.editDosage)

        nameEdit.setText(medicine.name)
        descEdit.setText(medicine.description)
        dosageEdit.setText(medicine.dosage)

        val editDialog = AlertDialog.Builder(this)
            .setTitle("Edit Medicine")
            .setView(dialogView)
            .setPositiveButton("Proceed", null)
            .setNegativeButton("Cancel", null)
            .create()

        editDialog.show()

        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val updatedName = nameEdit.text.toString()
            val updatedDesc = descEdit.text.toString()
            val updatedDosage = dosageEdit.text.toString()

            if (updatedName.isEmpty() || updatedDesc.isEmpty() || updatedDosage.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editDialog.dismiss()

            val timeCalendar = Calendar.getInstance()

            val existingTime = medicine.reminderTime
            if (existingTime != null) {
                val timeParts = existingTime.split(":")
                if (timeParts.size == 2) {
                    timeCalendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    timeCalendar.set(Calendar.MINUTE, timeParts[1].toInt())
                }
            }

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val newReminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                    val updatedMedicine = Medicine(
                        updatedName,
                        updatedDesc,
                        updatedDosage,
                        newReminderTime
                    )

                    updateMedicineAlarm(medicine.name, updatedMedicine)


                    medicineList[position] = updatedMedicine

                    adapter.notifyItemChanged(position)

                    saveMedicines()

                    Toast.makeText(
                        this,
                        "Medicine updated with reminder at $newReminderTime",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                timeCalendar.get(Calendar.HOUR_OF_DAY),
                timeCalendar.get(Calendar.MINUTE),
                true
            )

            timePickerDialog.show()
        }
    }

    private fun updateMedicineAlarm(oldMedicineName: String, updatedMedicine: Medicine) {
        val oldIntent = Intent(this, ReminderReceiver::class.java)
        oldIntent.putExtra("medicineName", oldMedicineName)
        val oldPendingIntent = PendingIntent.getBroadcast(
            this,
            oldMedicineName.hashCode(),
            oldIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(oldPendingIntent)

        val reminderTime = updatedMedicine.reminderTime ?: return
        val timeParts = reminderTime.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val timeCalendar = Calendar.getInstance()
        timeCalendar.set(Calendar.HOUR_OF_DAY, hour)
        timeCalendar.set(Calendar.MINUTE, minute)
        timeCalendar.set(Calendar.SECOND, 0)
        timeCalendar.set(Calendar.MILLISECOND, 0)

        if (timeCalendar.timeInMillis <= System.currentTimeMillis()) {
            timeCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val newIntent = Intent(this, ReminderReceiver::class.java)
        newIntent.putExtra("medicineName", updatedMedicine.name)
        val newPendingIntent = PendingIntent.getBroadcast(
            this,
            updatedMedicine.name.hashCode(),
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeCalendar.timeInMillis, newPendingIntent)
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeCalendar.timeInMillis, newPendingIntent)
        }
    }

    private fun setDailyReminder(calendar: Calendar, medicineName: String) {
        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("medicineName", medicineName)

        val requestCode = medicineName.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun showOverlayButtons(position: Int) {
        val overlay = findViewById<LinearLayout>(R.id.overlayButtons)
        overlay.visibility = View.VISIBLE

        val btnEdit = overlay.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = overlay.findViewById<ImageButton>(R.id.btnDelete)

        btnEdit.setOnClickListener {
            editMedicine(position)
            overlay.visibility = View.GONE
        }

        btnDelete.setOnClickListener {
            removeMedicine(position)
            overlay.visibility = View.GONE
        }

        findViewById<View>(android.R.id.content).setOnClickListener {
            overlay.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == REMINDER_ACTION_REQUEST_CODE) {
            val medicineName = data?.getStringExtra("medicineName")
            val newTime = data?.getStringExtra("newTime")
            val position = data?.getIntExtra("medicinePosition", -1) ?: -1

            if (medicineName != null && newTime != null && position >= 0 && position < medicineList.size) {
                val medicine = medicineList[position]

                val updatedMedicine = Medicine(
                    medicine.name,
                    medicine.description,
                    medicine.dosage,
                    newTime
                )

                medicineList[position] = updatedMedicine
                saveMedicines()
                adapter.notifyItemChanged(position)

                Toast.makeText(this, "Medicine reminder updated to $newTime", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update reminder time", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openReminderAction(medicineName: String, medicinePosition: Int = -1) {
        var position = medicinePosition
        var medicineDescription = ""
        var medicineDosage = ""

        if (position == -1) {
            for (i in medicineList.indices) {
                if (medicineList[i].name == medicineName) {
                    position = i
                    medicineDescription = medicineList[i].description
                    medicineDosage = medicineList[i].dosage
                    break
                }
            }
        } else if (position >= 0 && position < medicineList.size) {
            medicineDescription = medicineList[position].description
            medicineDosage = medicineList[position].dosage
        }

        if (position >= 0) {
            val intent = Intent(this, ReminderActionActivity::class.java)
            intent.putExtra("medicineName", medicineName)
            intent.putExtra("medicineDescription", medicineDescription)
            intent.putExtra("medicineDosage", medicineDosage)
            intent.putExtra("medicinePosition", position)
            startActivityForResult(intent, REMINDER_ACTION_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserDisplayName()

        if (::navProfilePicture.isInitialized) {
            ProfileManager.loadProfilePicture(this, navProfilePicture)
        }

        updateTodayMedications()
    }

    private fun updateUserDisplayName() {
        val sharedPrefs = getSharedPreferences("user_profile", MODE_PRIVATE)
        val firstName = sharedPrefs.getString("first_name", "User") ?: "User"
        helloUserText.text = "$firstName"
    }
}