package com.example.yourappname

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourappname.data.Medicine
import com.example.yourappname.helper.ProfileManager
import com.example.yourappname.helper.ThemeHelper
import com.example.yourappname.helper.TodayMedicationAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var helloUserText: TextView
    private lateinit var dateTextView: TextView
    private lateinit var viewAllMedsButton: Button
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var navProfilePicture: ImageView

    private lateinit var todayMedicationsRecyclerView: RecyclerView
    private lateinit var todayEmptyStateView: LinearLayout
    private lateinit var todayMedicationAdapter: TodayMedicationAdapter

    private lateinit var healthTipText: TextView

    private lateinit var profileButton: ImageButton
    private lateinit var medicationsButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var devButton: ImageButton

    private val medicineList = mutableListOf<Medicine>()
    private val todayMedicationsList = mutableListOf<Medicine>()

    private lateinit var themeHelper: ThemeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_ver2)


        themeHelper = ThemeHelper.attach(this)


        sharedPrefs = getSharedPreferences("med_prefs", MODE_PRIVATE)


        initializeViews()
        setupCurrentDate()
        loadMedicines()
        setupTodayMedicationsRecyclerView()
        updateTodayMedicationsView()
        setupClickListeners()
        updateUserDisplayName()
        setRandomHealthTip()
    }

    private fun initializeViews() {
        helloUserText = findViewById(R.id.helloUserText)
        dateTextView = findViewById(R.id.dateTextView)
        viewAllMedsButton = findViewById(R.id.viewAllMedsButton)
        healthTipText = findViewById(R.id.healthTipText)


        todayMedicationsRecyclerView = findViewById(R.id.todayMedicationsRecyclerView)
        todayEmptyStateView = findViewById(R.id.todayEmptyStateView)


        profileButton = findViewById(R.id.profileButton)
        medicationsButton = findViewById(R.id.medicationsButton)
        settingsButton = findViewById(R.id.settingsButton)
        devButton = findViewById(R.id.devButton)
    }

    private fun setupCurrentDate() {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        dateTextView.text = currentDate
    }

    private fun loadMedicines() {
        val gson = Gson()
        val json = sharedPrefs.getString("medicine_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Medicine>>() {}.type
            val savedList: List<Medicine> = gson.fromJson(json, type)
            medicineList.clear()
            medicineList.addAll(savedList)

            todayMedicationsList.clear()
            todayMedicationsList.addAll(medicineList)


            todayMedicationsList.sortBy { it.reminderTime }
        }
    }


    private fun setupTodayMedicationsRecyclerView() {
        todayMedicationsRecyclerView.layoutManager = LinearLayoutManager(this)
        todayMedicationAdapter = TodayMedicationAdapter(
            todayMedicationsList,
            this
        ) { medicine ->
            Toast.makeText(this, "Clicked on ${medicine.name}", Toast.LENGTH_SHORT).show()
        }
        todayMedicationsRecyclerView.adapter = todayMedicationAdapter
    }

    private fun updateTodayMedicationsView() {
        todayMedicationAdapter.notifyDataSetChanged()

        if (todayMedicationsList.isEmpty()) {
            todayEmptyStateView.visibility = View.VISIBLE
            todayMedicationsRecyclerView.visibility = View.GONE
        } else {
            todayEmptyStateView.visibility = View.GONE
            todayMedicationsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        viewAllMedsButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }


        profileButton.setOnClickListener {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        medicationsButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        devButton.setOnClickListener {
            startActivity(Intent(this, DeveloperActivity::class.java))
        }
    }

    private fun updateUserDisplayName() {
        val sharedPrefs = getSharedPreferences("user_profile", MODE_PRIVATE)
        val firstName = sharedPrefs.getString("first_name", "User") ?: "User"
        helloUserText.text = "$firstName!"
    }

    private fun setRandomHealthTip() {
        val healthTips = arrayOf(
            "Taking medications with food helps reduce stomach irritation and improves absorption for many medications.",
            "Set alarms or reminders to help stay on track with your medication schedule.",
            "Store medications in a cool, dry place away from direct sunlight.",
            "Always complete your full course of antibiotics, even if you feel better.",
            "Keep a medication log to track your daily intake and any side effects.",
            "Don't crush or split pills unless specifically instructed by your doctor.",
            "Check expiration dates regularly and dispose of expired medications properly.",
            "Inform all your healthcare providers about all medications you're taking.",
            "Over-the-counter medications can interact with prescription drugs - always check with your pharmacist.",
            "Stay hydrated when taking medications unless instructed otherwise by your doctor."
        )

        val randomIndex = (0 until healthTips.size).random()
        healthTipText.text = healthTips[randomIndex]
    }

    override fun onResume() {
        super.onResume()
        loadMedicines()
        updateTodayMedicationsView()

        updateUserDisplayName()

        if (::navProfilePicture.isInitialized) {
            ProfileManager.loadProfilePicture(this, navProfilePicture)
        }
    }
}