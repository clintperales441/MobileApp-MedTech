package com.example.yourappname.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourappname.R
import com.example.yourappname.data.Medicine

class MedicineAdapter(
    private val medicineList: List<Medicine>,
    private val editAction: (Int) -> Unit,
    private val removeAction: (Int) -> Unit,
    private val longPressAction: (Int) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.medicineNameText)
        val descText: TextView = itemView.findViewById(R.id.medicineDescText)
        val dosageText: TextView = itemView.findViewById(R.id.medicineDosageText)
        val timeText: TextView = itemView.findViewById(R.id.reminderTimeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicineList[position]

        holder.nameText.text = medicine.name
        holder.descText.text = medicine.description
        holder.dosageText.text = medicine.dosage
        holder.timeText.text = medicine.reminderTime ?: "No reminder"


        holder.itemView.setOnClickListener {
            editAction(position)
        }


        holder.itemView.setOnLongClickListener {
            longPressAction(position)
            true
        }
    }

    override fun getItemCount(): Int = medicineList.size
}