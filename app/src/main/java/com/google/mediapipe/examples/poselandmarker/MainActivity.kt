package com.google.mediapipe.examples.poselandmarker

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
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
                R.id.mainScreenFragment -> activityMainBinding.navigation.visibility = View.GONE
                R.id.summary_fragment -> activityMainBinding.navigation.visibility = View.GONE
                R.id.camera_fragment, R.id.gallery_fragment -> activityMainBinding.navigation.visibility =
                    View.VISIBLE

                else -> activityMainBinding.navigation.visibility = View.VISIBLE
            }
        }
        // Handle the back button event
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
}
