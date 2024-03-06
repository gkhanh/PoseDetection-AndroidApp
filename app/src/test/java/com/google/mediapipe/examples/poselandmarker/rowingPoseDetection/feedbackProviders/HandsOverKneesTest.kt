package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.HandsOverKnees
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandsOverKneesTest {
    @Test
    fun test_HandsOverKneeRules(){
        /* Given */
        // Feedback should be empty in this case:
//        val lastShoulderAngleDuringDrive = 25f
//        val lastElbowAngleDuringDrive = 140f
//        val previousWristXCoordinateDuringDrive = 0.56f
//        val lastKneeXCoordinateDuringDrive = 0.32f
//        val lastWristXCoordinateDuringDrive = 0.42f
//        val lastKneeAngleDuringDrive = 140f

        // feedback should be "Hands not over knees"
        val lastShoulderAngleDuringDrive = 25f
        val lastElbowAngleDuringDrive = 80f
        val previousWristXCoordinateDuringDrive = 0.56f
        val lastKneeXCoordinateDuringDrive = 0.52f
        val lastWristXCoordinateDuringDrive = 0.42f
        val lastKneeAngleDuringDrive = 130f


        /* When */
        val feedbackMessage = HandsOverKnees().analyzeData(lastShoulderAngleDuringDrive, lastElbowAngleDuringDrive, previousWristXCoordinateDuringDrive, lastKneeXCoordinateDuringDrive, lastWristXCoordinateDuringDrive, lastKneeAngleDuringDrive)

        /* Then */
        assertEquals(listOf("Not pulling arm"), feedbackMessage)
        assertTrue("Feedback message should be empty", feedbackMessage.isEmpty())
        assertEquals(listOf("Hands not over knees"), feedbackMessage)
        assertEquals(listOf("Arm not pulled back properly"), feedbackMessage)
    }
}