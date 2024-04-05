package com.epi.epilog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.epi.epilog.databinding.ActivitySeizureDetailBinding
import com.epi.epilog.databinding.ActivitySeizureEditBinding
import com.epi.epilog.databinding.SeizureEdit10Binding
import com.epi.epilog.databinding.SeizureEdit2Binding
import com.epi.epilog.databinding.SeizureEdit4Binding
import com.epi.epilog.databinding.SeizureEdit5Binding
import com.epi.epilog.databinding.SeizureEdit6Binding
import com.epi.epilog.databinding.SeizureEdit8Binding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySeizureEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val seizureFragment = SeizureFragment()
        transaction.add(R.id.seizure_edit_fragment, seizureFragment)
        transaction.commit()
    }
}

class SeizureFragment: Fragment() {
    lateinit var binding: SeizureEdit2Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SeizureEdit2Binding.inflate(inflater, container, false)
        return binding.root
    }
}