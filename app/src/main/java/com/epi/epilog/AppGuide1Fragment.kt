package com.epi.epilog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epi.epilog.databinding.FragmentAppGuide1Binding


class AppGuide1Fragment : Fragment() {

    lateinit var binding: FragmentAppGuide1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppGuide1Binding.inflate(inflater, container,false)

        return binding?.root
    }

}