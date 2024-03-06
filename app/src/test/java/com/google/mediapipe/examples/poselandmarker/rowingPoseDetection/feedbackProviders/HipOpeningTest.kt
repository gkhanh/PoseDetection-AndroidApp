package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.HipOpening
import org.junit.Assert.assertEquals
import org.junit.Test

class HipOpeningTest {
    @Test
    fun test_HipOpeningRule(){
        /* Given */
        // this should pass the test
        val previousHipAngleDuringDrive = 70f
        val lastHipAngleDuringDrive = 80f
        val lastKneeAngleDuringDrive = 120f

        /* When */
        val feedbackMessage = HipOpening().analyzeData(previousHipAngleDuringDrive, lastHipAngleDuringDrive, lastKneeAngleDuringDrive)

        /* Then */
        assertEquals(listOf("Hip is not open"), feedbackMessage)
    }


}