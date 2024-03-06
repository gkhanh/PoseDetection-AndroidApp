package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class BodyPosture : RowingFeedbackProvider.FeedbackProvider {

    private fun extractData(normalizedFrameMeasurements: List<NormalizedFrameMeasurement>): List<Float?> {
        val firstFrameMeasurement =
            normalizedFrameMeasurements[normalizedFrameMeasurements.size - 4]
        val lastFrameMeasurement = normalizedFrameMeasurements.last()
        val previousHipAngleDuringRecovery =
            CalculateAngles().calculateHipAngle(firstFrameMeasurement)
        val lastHipAngleDuringRecovery = CalculateAngles().calculateHipAngle(lastFrameMeasurement)
        val previousElbowAngleDuringRecovery =
            CalculateAngles().calculateElbowAngle(firstFrameMeasurement)
        val lastElbowAngleDuringRecovery =
            CalculateAngles().calculateElbowAngle(lastFrameMeasurement)
        return listOf(
            previousHipAngleDuringRecovery,
            lastHipAngleDuringRecovery,
            previousElbowAngleDuringRecovery,
            lastElbowAngleDuringRecovery
        )
    }

    fun analyzeData(
        previousHipAngleDuringRecovery: Float?,
        lastHipAngleDuringRecovery: Float?,
        previousElbowAngleDuringRecovery: Float?,
        lastElbowAngleDuringRecovery: Float?
    ): MutableList<String> {
        val feedback = mutableListOf<String>()
        if (lastElbowAngleDuringRecovery != null && previousElbowAngleDuringRecovery != null && lastElbowAngleDuringRecovery > previousElbowAngleDuringRecovery &&
            previousHipAngleDuringRecovery != null && lastHipAngleDuringRecovery != null && lastHipAngleDuringRecovery > previousHipAngleDuringRecovery &&
            !(lastHipAngleDuringRecovery < 90)
        ) {
            feedback.add("Tip your body forward")
        }
        return feedback
    }

    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.DRIVE_PHASE) {
            val (previousHipAngleDuringRecovery, lastHipAngleDuringRecovery, previousElbowAngleDuringRecovery, lastElbowAngleDuringRecovery) = extractData(
                frameMeasurementBuffer
            )
            return analyzeData(
                previousHipAngleDuringRecovery,
                lastHipAngleDuringRecovery,
                previousElbowAngleDuringRecovery,
                lastElbowAngleDuringRecovery
            )
        }
        return emptyList()
    }
}