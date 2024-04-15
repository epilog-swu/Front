package com.epi.epilog

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epi.epilog.databinding.FragmentAppGuide1Binding
import com.epi.epilog.databinding.FragmentAppGuide3Binding


class AppGuide3Fragment : Fragment() {

    lateinit var binding: FragmentAppGuide3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppGuide3Binding.inflate(inflater, container,false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.GotoStartPageBtn.setOnClickListener {
            val intent: Intent = Intent(requireContext(), StartActivity::class.java)
            startActivity(intent)
        }
    }


}