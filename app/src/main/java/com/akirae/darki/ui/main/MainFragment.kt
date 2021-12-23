package com.akirae.darki.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.akirae.darki.R
import com.akirae.darki.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.encrypt.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_encryptFragment)
        }
        binding.decrypt.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_decryptFragment)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}