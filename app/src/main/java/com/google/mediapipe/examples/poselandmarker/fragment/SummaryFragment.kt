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
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {
    private var _fragmentSummaryBinding: FragmentSummaryBinding? = null
    private val fragmentSummaryBinding
        get() = _fragmentSummaryBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSummaryBinding =
            FragmentSummaryBinding.inflate(inflater, container, false)
        return fragmentSummaryBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor =
            requireContext().getColor(R.color.status_bar_white)
        // Next, set the status bar icons to be dark for better contrast:
        requireActivity().window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
        val strokeCount = arguments?.getInt("strokeCount") ?: 0 //if null then strokeCount is 0
        val correctStrokePercentage = arguments?.getInt("correctStrokePercentage") ?: 0
        fragmentSummaryBinding.strokeCountNumber.text =
            getString(R.string.total_stroke_count, strokeCount)
        fragmentSummaryBinding.CorrectPercentage.text =
            getString(R.string.correct_percentage, correctStrokePercentage)
        fragmentSummaryBinding.CommonMistake.text =
            getString(R.string.most_common_mistake, arguments?.getString("mostCommonFeedback"))
        fragmentSummaryBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_summary_fragment_to_mainScreenFragment)
        }
    }

}