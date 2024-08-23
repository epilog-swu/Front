package com.epi.epilog.medicine

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.ChecklistItem
import com.epi.epilog.api.State

class MedicineAdapter(
    private var checklist: MutableList<ChecklistItem>,
    private val onItemClicked: (ChecklistItem) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineTime: TextView = itemView.findViewById(R.id.medicine_time)
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val medicineCheckbox: CheckBox = itemView.findViewById(R.id.medicine_checkbox)

        fun bind(item: ChecklistItem, onItemClicked: (ChecklistItem) -> Unit) {
            medicineTime.text = item.time
            medicineName.text = item.medicationName
            medicineCheckbox.isChecked = item.state == State.복용

            // UI update based on state
            when (item.state) {
                State.복용 -> {
                    applyStrikeThrough(medicineName, medicineTime, true)
                    itemView.setBackgroundResource(R.drawable.medicine_background)
                }
                State.미복용, State.상태없음 -> {
                    applyStrikeThrough(medicineName, medicineTime, false)
                    itemView.setBackgroundResource(if (item.state == State.미복용) R.drawable.background_red else R.drawable.background_yellow)
                }
            }

            itemView.setOnClickListener { onItemClicked(item) }
            medicineCheckbox.setOnClickListener { onItemClicked(item) }

            // Disable checkbox click
            medicineCheckbox.isClickable = false
        }

        private fun applyStrikeThrough(medicineName: TextView, medicineTime: TextView, isComplete: Boolean) {
            if (isComplete) {
                medicineName.paintFlags = medicineName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                medicineTime.paintFlags = medicineTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                medicineName.paintFlags = medicineName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                medicineTime.paintFlags = medicineTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_medicine_checklist_item, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(checklist[position], onItemClicked)
    }

    override fun getItemCount(): Int = checklist.size

    // Method to update the checklist and notify the adapter
    fun updateChecklist(newChecklist: MutableList<ChecklistItem>) {
        this.checklist = newChecklist
        notifyDataSetChanged()
    }
}
