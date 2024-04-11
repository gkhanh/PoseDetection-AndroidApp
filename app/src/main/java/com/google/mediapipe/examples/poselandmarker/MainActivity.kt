package com.google.mediapipe.examples.poselandmarker

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        activityMainBinding.navigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.camera_fragment, R.id.gallery_fragment -> adjustFragmentContainerLayout(true)
                else -> adjustFragmentContainerLayout(false)
            }
        }
        // Handle the back button event
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
    private fun adjustFragmentContainerLayout(isNavigationBarVisible: Boolean) {
        val params = activityMainBinding.fragmentContainer.layoutParams as ConstraintLayout.LayoutParams
        if (isNavigationBarVisible) {
            // When navigation is visible, constrain the bottom of the FragmentContainerView to the top of the navigation bar
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToTop = activityMainBinding.navigation.id
        } else {
            // When navigation is hidden, constrain the bottom of the FragmentContainerView to the bottom of the parent
            params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }
        activityMainBinding.fragmentContainer.layoutParams = params
        activityMainBinding.navigation.visibility = if (isNavigationBarVisible) View.VISIBLE else View.GONE
    }
}
