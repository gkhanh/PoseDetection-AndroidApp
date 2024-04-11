package com.google.mediapipe.examples.poselandmarker.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentMainScreenBinding

class MainScreenFragment : Fragment() {
    private var _fragmentMainScreenBinding: FragmentMainScreenBinding? = null
    private val fragmentMainScreenBinding
        get() = _fragmentMainScreenBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentMainScreenBinding = FragmentMainScreenBinding.inflate(inflater, container, false)
        return fragmentMainScreenBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = requireContext().getColor(R.color.status_bar_black)
        requireActivity().window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        fragmentMainScreenBinding.button.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenFragment_to_camera_fragment)
        }
    }
}