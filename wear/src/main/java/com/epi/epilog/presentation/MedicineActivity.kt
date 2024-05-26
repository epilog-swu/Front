package com.epi.epilog.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R
import java.time.LocalDate

class MedicineActivity : ComponentActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val myDataset = arrayOf("약 예시1", "약 예시2", "약 예시3", "약 예시4", "약 예시5", "약 예시6")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        //오늘의 날짜 정보 받기
        val selectedDate = intent.getStringExtra("SELECTED_DATE")?.let {
            LocalDate.parse(it)
        }
        if (selectedDate != null) {
            // 날짜 정보를 제대로 받았는지 로그와 토스트 메시지로 확인
            Log.d("MedicineActivity", "Received date: $selectedDate")
        } else {
            Log.e("MedicineActivity", "No date received")
        }


        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset, this)

        recyclerView = findViewById<RecyclerView>(R.id.wearable_recycler_view_medicine).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    fun showDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_checklist_dialog_2, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        with(builder) {
            val dialog = create()

            dialogView.findViewById<Button>(R.id.dialog_button_yes).setOnClickListener {
                // 현재 시각에 밥을 먹음
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_no).setOnClickListener {
                // 제시간에 밥을 먹음
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.dialog_button_pass).setOnClickListener {
                // 식사를 건너뜀
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    class MyAdapter(private val myDataset: Array<String>, private val context: MedicineActivity) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        class MyViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val checkBox = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_item, parent, false) as CheckBox
            return MyViewHolder(checkBox)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = myDataset[position]
            holder.checkBox.text = item
            holder.checkBox.setOnClickListener {
                if (holder.checkBox.isChecked) {
                    context.showDialog()
                }
            }
        }

        override fun getItemCount() = myDataset.size
    }
}
