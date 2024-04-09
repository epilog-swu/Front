package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
<<<<<<< HEAD
import androidx.compose.material3.ModalBottomSheet
import androidx.fragment.app.DialogFragment
import com.epi.epilog.databinding.ActivitySeizureDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
=======
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.epi.epilog.databinding.ActivityModeSelectBinding
import com.epi.epilog.databinding.ActivitySeizureDetailBinding
import com.epi.epilog.databinding.ActivitySeizureEditBinding
import com.epi.epilog.databinding.ActivityStartBinding
import com.epi.epilog.databinding.SeizureEdit11Binding
import com.epi.epilog.databinding.SeizureEdit1Binding
import com.epi.epilog.databinding.SeizureEdit2Binding
import com.epi.epilog.databinding.SeizureEdit3Binding
import com.epi.epilog.databinding.SeizureEdit4Binding
import com.epi.epilog.databinding.SeizureEdit7Binding
import com.epi.epilog.databinding.SeizureEdit9Binding
import com.epi.epilog.databinding.SignUp1Binding
import com.epi.epilog.databinding.SignUp2Binding
import com.epi.epilog.databinding.SignUp3Binding
import com.epi.epilog.databinding.SignUp4Binding
import com.epi.epilog.databinding.SignUp5Binding

>>>>>>> main

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< HEAD
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
=======
        val binding = ActivitySeizureEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val seizureFragment = SeizureFragment()
        transaction.add(R.id.seizure_edit_fragment, seizureFragment)
        transaction.commit()
>>>>>>> main
    }

<<<<<<< HEAD
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
=======
class SeizureFragment: Fragment() {
    lateinit var binding: SeizureEdit9Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SeizureEdit9Binding.inflate(inflater, container, false)
        return binding.root
    }
}

class SignUp1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp1Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class SignUp2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

class SignUp3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp3Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

class SignUp4Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp4Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
class SignUp5Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SignUp5Binding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}

>>>>>>> main
