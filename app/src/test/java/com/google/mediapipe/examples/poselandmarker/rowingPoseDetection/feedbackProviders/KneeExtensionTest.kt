package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.KneeExtension
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KneeExtensionTest {
    @Test
    fun test_KneeExtensionRule() {
        /* Given */
        // Feedback should be "Keep your back straight when extending legs"
        val previousKneeAngleDuringDrive = 162f
        val lastKneeAngleDuringDrive = 170f
        val previousHipAngleDuringDrive = 80f
        val lastHipAngleDuringDrive = 75f

        // Feedback should be empty
//        val previousKneeAngleDuringDrive = 160f
//        val lastKneeAngleDuringDrive = 130f
//        val previousHipAngleDuringDrive = 90f
//        val lastHipAngleDuringDrive = 60f

        /* When */
        val feedbackMessage = KneeExtension().analyzeData(previousHipAngleDuringDrive, lastHipAngleDuringDrive, previousKneeAngleDuringDrive, lastKneeAngleDuringDrive)

        /* Then */
        // Case 1
        assertEquals(listOf("Keep your back straight when extending legs"), feedbackMessage)
        // Case 2
        // assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())
    }
}