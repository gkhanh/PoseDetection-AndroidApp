package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.BodyPosture
import org.junit.Assert.assertEquals
import org.junit.Test

class BodyPostureTest {

    @Test
    fun test_BodyPostureRule(){
        /* Given */

        // This values should show message "Tip your body forward"
        val previousHipAngleDuringRecovery = 130f
        val lastHipAngleDuringRecovery = 140f
        val previousElbowAngleDuringRecovery = 70f
        val lastElbowAngleDuringRecovery = 170f

        /* When */
        val feedbackMessage = BodyPosture().analyzeData(previousHipAngleDuringRecovery, lastHipAngleDuringRecovery, previousElbowAngleDuringRecovery, lastElbowAngleDuringRecovery)

        /* Then */
        assertEquals(listOf("Tip your body forward"), feedbackMessage)
    }

}