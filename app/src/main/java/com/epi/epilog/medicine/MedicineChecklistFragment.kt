package com.epi.epilog.medicine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.epi.epilog.signup.LoginActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import com.epi.epilog.api.ChecklistItem
import com.epi.epilog.api.MedicationChecklistResponse
import com.epi.epilog.api.RetrofitClient
import com.epi.epilog.api.State
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate

class MedicineChecklistFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private lateinit var weekCalendarView: WeekCalendarView
    private var medicationId: Int? = null
    private lateinit var medicineAdapter: MedicineAdapter
    private var checklistItems: MutableList<ChecklistItem> = mutableListOf()
    private lateinit var instructionTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicine_checklist, container, false)

        view.findViewById<Button>(R.id.add_existing_medicine_button).setOnClickListener {
            val intent = Intent(context, MedicineDetailActivity::class.java)
            if (medicationId != null) {
                intent.putExtra("medicationId", medicationId)
                startActivity(intent)
            } else {
                Toast.makeText(context, "No medication ID found.", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.add_medicine_button).setOnClickListener {
            startActivity(Intent(context, MedicineAddModifyActivity::class.java))
        }

        instructionTextView = view.findViewById(R.id.instruction_text_view)

        val recyclerView: RecyclerView = view.findViewById(R.id.medicine_content_layout)
        recyclerView.layoutManager = LinearLayoutManager(context)

        medicineAdapter = MedicineAdapter(checklistItems) { item ->
            val bottomSheetFragment = MedicineBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt("checklist_item_id", item.id)  // Pass the item ID to the BottomSheet
                }
            }
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        recyclerView.adapter = medicineAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validateToken()

        weekCalendarView = view.findViewById(R.id.meal_calendarView)
        initWeekCalendarView()
    }

    private fun validateToken() {
        val token = getTokenFromSession()
        if (token.isEmpty()) {
            redirectToLogin()
            return
        }
        RetrofitClient.retrofitService.testApi("Bearer $token").enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    selectDate(LocalDate.now()) // Load data for today's date
                } else {
                    redirectToLogin()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                redirectToLogin()
            }
        })
    }

    private fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun getTokenFromSession(): String {
        val sharedPreferences = context?.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("AuthToken", "") ?: ""
    }

    private fun selectDate(date: LocalDate) {
        selectedDate = date
        fetchMedicationChecklist(date)
    }

    private fun fetchMedicationChecklist(date: LocalDate) {
        val dateString = date.toString()
        val token = "Bearer " + getTokenFromSession()

        RetrofitClient.retrofitService.getMedicationChecklist(dateString, token).enqueue(object :
            Callback<MedicationChecklistResponse> {
            override fun onResponse(call: Call<MedicationChecklistResponse>, response: Response<MedicationChecklistResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { fetchedResponse ->
                        medicationId = fetchedResponse.medicationId // Set medicationId

                        checklistItems.clear()
                        checklistItems.addAll(fetchedResponse.checklist.filter { it.goalTime.startsWith(dateString) })
                        medicineAdapter.notifyDataSetChanged()

                        // Check if checklistItems is empty and update the visibility of instructionTextView
                        if (checklistItems.isEmpty()) {
                            instructionTextView.visibility = View.GONE
                        } else {
                            instructionTextView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to load medication checklist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MedicationChecklistResponse>, t: Throwable) {
                Toast.makeText(context, "Error fetching checklist: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun applyStateChangeToMedicineItem(medicineItemId: Int, newState: State) {
        val itemIndex = checklistItems.indexOfFirst { it.id == medicineItemId }
        if (itemIndex != -1) {
            checklistItems[itemIndex].state = newState
            medicineAdapter.notifyItemChanged(itemIndex) // Notify the adapter of the specific item change
        } else {
            // 만약 아이템이 없다면 로드 후 갱신
            fetchMedicationChecklist(selectedDate!!)
        }
    }

    private fun initWeekCalendarView() {
        weekCalendarView.dayBinder = object :
            WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.date == selectedDate) {
                    container.textView.setBackgroundResource(R.drawable.app_week_cal_selected_date)
                } else {
                    container.textView.setBackgroundResource(0) // Default background
                }

                container.textView.setOnClickListener {
                    onDateSelected(data.date)
                }
            }
        }

        val currentDate = LocalDate.now()
        weekCalendarView.setup(
            currentDate.minusMonths(100).withDayOfMonth(1),
            currentDate.plusMonths(100).withDayOfMonth(currentDate.lengthOfMonth()),
            DayOfWeek.SUNDAY
        )
        weekCalendarView.scrollToWeek(currentDate)
    }

    private fun onDateSelected(date: LocalDate) {
        val currentSelection = selectedDate
        selectedDate = date

        weekCalendarView.notifyDateChanged(date)
        if (currentSelection != null && currentSelection != date) {
            weekCalendarView.notifyDateChanged(currentSelection)
        }

        Toast.makeText(context, "선택된 날짜: $date", Toast.LENGTH_SHORT).show()

        selectDate(date)
    }

    private class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.weekcalendarDayText)
    }
}
