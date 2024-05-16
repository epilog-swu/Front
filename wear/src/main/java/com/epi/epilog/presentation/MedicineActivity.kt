package com.epi.epilog.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epi.epilog.R


class MedicineActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val myDataset = arrayOf("약 예시1", "약 예시2", "약 예시3", "약 예시4", "약 예시5", "약 예시6")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine)

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(myDataset, this)

        recyclerView = findViewById<RecyclerView>(R.id.wearable_recycler_view_medicine).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
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
                if (holder.checkBox.isChecked == true){
                    // 서버에 체크신호 보내기
                }
            }
        }

        override fun getItemCount() = myDataset.size
    }
}