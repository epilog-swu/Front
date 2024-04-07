package com.epi.epilog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.epi.epilog.databinding.ActivitySeizureDetailBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val binding = ActivitySeizureDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.main_calendar_1)

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