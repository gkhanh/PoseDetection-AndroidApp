package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.ArmAndLegMovement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArmAndLegMovementTest {
    @Test
    fun test_ArmAndLegMovementRule(){
        /* Given */
        // This should show nothing
//        val lastElbowAngleDuringRecovery = 50f
//        val previousElbowAngleDuringRecovery = 40f
//        val lastKneeAngleDuringRecovery = 160f
//        val previousKneeAngleDuringRecovery = 170f
//        val lastKneeXCoordinateDuringRecovery = 0.63f
//        val previousWristXCoordinateDuringRecovery = 0.22f
//        val lastWristXCoordinateDuringRecovery = 0.26f

        // This should show the message "Move the handle forward"
//        val lastElbowAngleDuringRecovery = 30f
//        val previousElbowAngleDuringRecovery = 40f
//        val lastKneeAngleDuringRecovery = 160f
//        val previousKneeAngleDuringRecovery = 170f
//        val lastKneeXCoordinateDuringRecovery = 0.63f
//        val previousWristXCoordinateDuringRecovery = 0.24f
//        val lastWristXCoordinateDuringRecovery = 0.20f

        // This should shows the message "Straighten the arm"
        val lastElbowAngleDuringRecovery = 140f
        val previousElbowAngleDuringRecovery = 50f
        val lastKneeAngleDuringRecovery = 160f
        val previousKneeAngleDuringRecovery = 170f
        val lastKneeXCoordinateDuringRecovery = 0.43f
        val previousWristXCoordinateDuringRecovery = 0.24f
        val lastWristXCoordinateDuringRecovery = 0.45f

        /* When */
        val feedbackMessage = ArmAndLegMovement().analyzeData(lastElbowAngleDuringRecovery, previousElbowAngleDuringRecovery, lastKneeAngleDuringRecovery, previousKneeAngleDuringRecovery, lastKneeXCoordinateDuringRecovery, previousWristXCoordinateDuringRecovery, lastWristXCoordinateDuringRecovery)

        /* Then */
        //assertEquals(listOf("Move the handle forward"), feedbackMessage)
        assertEquals(listOf("Straighten the arm"), feedbackMessage)
        // assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())

    }
}