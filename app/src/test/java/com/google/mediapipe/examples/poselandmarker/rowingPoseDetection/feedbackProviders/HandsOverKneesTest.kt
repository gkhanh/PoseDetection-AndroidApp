package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.HandsOverKnees
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandsOverKneesTest {
    @Test
    fun test_HandsOverKneeRules(){
        /* Given */
        // Feedback should be "Not pulling arm"
//        val lastShoulderAngleDuringDrive = 25f
//        val lastElbowAngleDuringDrive = 80f
//        val previousWristXCoordinateDuringDrive = 0.53f
//        val lastKneeXCoordinateDuringDrive = 0.52f
//        val lastWristXCoordinateDuringDrive = 0.47f
//        val lastKneeAngleDuringDrive = 130f

        // Feedback should be empty in this case:
//        val lastShoulderAngleDuringDrive = 30f
//        val lastElbowAngleDuringDrive = 55f
//        val previousWristXCoordinateDuringDrive = 0.31f
//        val lastKneeXCoordinateDuringDrive = 0.32f
//        val lastWristXCoordinateDuringDrive = 0.26f
//        val lastKneeAngleDuringDrive = 160f

        // Feedback should be "Hands not over knees"
//        val lastShoulderAngleDuringDrive = 25f
//        val lastElbowAngleDuringDrive = 80f
//        val previousWristXCoordinateDuringDrive = 0.56f
//        val lastKneeXCoordinateDuringDrive = 0.47f
//        val lastWristXCoordinateDuringDrive = 0.6f
//        val lastKneeAngleDuringDrive = 130f

        // Feedback should be "Arm not pulled back properly"
        val lastShoulderAngleDuringDrive = 255f
        val lastElbowAngleDuringDrive = 76f
        val previousWristXCoordinateDuringDrive = 0.56f
        val lastKneeXCoordinateDuringDrive = 0.5f
        val lastWristXCoordinateDuringDrive = 0.41f
        val lastKneeAngleDuringDrive = 140f


        /* When */
        val feedbackMessage = HandsOverKnees().analyzeData(lastShoulderAngleDuringDrive, lastElbowAngleDuringDrive, previousWristXCoordinateDuringDrive, lastKneeXCoordinateDuringDrive, lastWristXCoordinateDuringDrive, lastKneeAngleDuringDrive)

        /* Then */
        // Case 1
        // assertEquals(listOf("Not pulling arm"), feedbackMessage)
        // Case 2
        // assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())
        // Case 3
        // assertEquals(listOf("Hands not over knees"), feedbackMessage)
        // Case 4
        assertEquals(listOf("Arm not pulled back properly"), feedbackMessage)
    }
}