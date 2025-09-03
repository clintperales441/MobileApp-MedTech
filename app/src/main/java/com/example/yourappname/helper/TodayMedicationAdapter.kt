package com.example.yourappname.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourappname.R
import com.example.yourappname.data.Medicine
import java.text.SimpleDateFormat
import java.util.*

class TodayMedicationAdapter(
    private val medicationsList: List<Medicine>,
    private val context: Context,
    private val onItemClick: (Medicine) -> Unit = {}
) : RecyclerView.Adapter<TodayMedicationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.medicationTimeText)
        val nameText: TextView = itemView.findViewById(R.id.medicationNameText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_medication, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicationsList[position]

        val timeStr = medicine.reminderTime ?: "00:00"
        try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(timeStr)
            val formattedTime = date?.let { outputFormat.format(it) } ?: timeStr
            holder.timeText.text = formattedTime
        } catch (e: Exception) {
            holder.timeText.text = timeStr
        }

        holder.nameText.text = "${medicine.name} ${medicine.dosage}"

        holder.itemView.setOnClickListener {
            onItemClick(medicine)
        }




    }

    override fun getItemCount(): Int = medicationsList.size
}