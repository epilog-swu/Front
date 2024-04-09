package com.epi.epilog

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ModalBottomSheet
import androidx.fragment.app.DialogFragment
import com.epi.epilog.databinding.ActivitySeizureDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val binding = ActivitySeizureDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.test)

//        val bottomSheetView = layoutInflater.inflate(R.layout.test, null)
//        val bottomSheetDialog = BottomSheetDialog(this)
//        bottomSheetDialog.setContentView(bottomSheetView)
//
//        findViewById<Button>(R.id.test).setOnClickListener {
//            bottomSheetDialog.show()
//        }
//        val fragmentManager: FragmentManager = supportFragmentManager
//        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
//        val seizureFragment = SeizureFragment()
//        transaction.add(R.id.seizure_edit_fragment, seizureFragment)
//        transaction.commit()
    }

}
//
//class SeizureFragment: Fragment() {
//    lateinit var binding: SeizureEdit2Binding
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = SeizureEdit2Binding.inflate(inflater, container, false)
//        return binding.root
//    }
//}