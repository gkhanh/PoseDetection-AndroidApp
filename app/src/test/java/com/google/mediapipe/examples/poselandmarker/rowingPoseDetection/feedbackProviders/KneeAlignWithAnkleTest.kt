package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.KneeAlignWithAnkle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KneeAlignWithAnkleTest {
    @Test
    fun test_KneeAlignWithAnkleRule() {
        /* Given */
        // Feedback should be "Knee must align with ankle"
//        val previousKneeAngleDuringRecovery = 110f
//        val lastKneeAngleDuringRecovery = 95f
//        val lastKneeXCoordinateDuringRecovery = 0.58f
//        val lastAnkleXCoordinateDuringRecovery = 0.73f

        // Feedback should be empty
        val previousKneeAngleDuringRecovery = 85f
        val lastKneeAngleDuringRecovery = 67f
        val lastKneeXCoordinateDuringRecovery = 0.75f
        val lastAnkleXCoordinateDuringRecovery = 0.76f

        /* When */
        val feedbackMessage = KneeAlignWithAnkle().analyzeData(
            previousKneeAngleDuringRecovery,
            lastKneeAngleDuringRecovery,
            lastKneeXCoordinateDuringRecovery,
            lastAnkleXCoordinateDuringRecovery
        )
        /* Then */
        // Case 1
        // assertEquals(listOf("Knee must align with ankle"), feedbackMessage)
        // Case 2
        assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())
    }
}