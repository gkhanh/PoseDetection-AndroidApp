package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.ArmAndLegMovement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArmAndLegMovementTest {
    @Test
    fun test_ArmAndLegMovementRule(){
        /* Given */
        // This should shows the message "Extend your arms forward during recovery."
//        val lastElbowAngleDuringRecovery = 180f
//        val previousElbowAngleDuringRecovery = 50f
//        val lastKneeAngleDuringRecovery = 160f
//        val previousKneeAngleDuringRecovery = 155f
//        val lastKneeXCoordinateDuringRecovery = 0.43f
//        val previousWristXCoordinateDuringRecovery = 0.44f
//        val lastWristXCoordinateDuringRecovery = 0.45f

        // This should show the message "Fully extend your arms during recovery."
//        val lastElbowAngleDuringRecovery = 130f
//        val previousElbowAngleDuringRecovery = 40f
//        val lastKneeAngleDuringRecovery = 160f
//        val previousKneeAngleDuringRecovery = 170f
//        val lastKneeXCoordinateDuringRecovery = 0.63f
//        val previousWristXCoordinateDuringRecovery = 0.24f
//        val lastWristXCoordinateDuringRecovery = 0.29f

        // This should show nothing
        val lastElbowAngleDuringRecovery = 180f
        val previousElbowAngleDuringRecovery = 50f
        val lastKneeAngleDuringRecovery = 160f
        val previousKneeAngleDuringRecovery = 170f
        val lastKneeXCoordinateDuringRecovery = 0.43f
        val previousWristXCoordinateDuringRecovery = 0.24f
        val lastWristXCoordinateDuringRecovery = 0.45f

        // This should show the message "Focus on extending your arms forward before bending your knees during recovery." and "Extend your arms forward during recovery."
//        val lastElbowAngleDuringRecovery = 160f
//        val previousElbowAngleDuringRecovery = 40f
//        val lastKneeAngleDuringRecovery = 160f
//        val previousKneeAngleDuringRecovery = 170f
//        val lastKneeXCoordinateDuringRecovery = 0.63f
//        val previousWristXCoordinateDuringRecovery = 0.22f
//        val lastWristXCoordinateDuringRecovery = 0.23f

        /* When */
        val feedbackMessage = ArmAndLegMovement().analyzeData(lastElbowAngleDuringRecovery, previousElbowAngleDuringRecovery, lastKneeAngleDuringRecovery, previousKneeAngleDuringRecovery, lastKneeXCoordinateDuringRecovery, previousWristXCoordinateDuringRecovery, lastWristXCoordinateDuringRecovery)

        /* Then */
        // Case 1
        // assertEquals(listOf("Extend your arms forward during recovery."), feedbackMessage)
        // Case 2
        // assertEquals(listOf("Fully extend your arms during recovery."), feedbackMessage)
        // Case 3
        assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())
        // Case 4
        // assertEquals(listOf("Extend your arms forward during recovery.", "Focus on extending your arms forward before bending your knees during recovery."), feedbackMessage)
    }
}