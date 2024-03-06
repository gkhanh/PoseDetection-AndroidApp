package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class ArmAndLegMovement : RowingFeedbackProvider.FeedbackProvider {
    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.DRIVE_PHASE) {
            val data = extractData(frameMeasurementBuffer)
            val lastElbowAngleDuringRecovery = data[0]
            val previousElbowAngleDuringRecovery = data[1]
            val lastKneeAngleDuringRecovery = data[2]
            val previousKneeAngleDuringRecovery = data[3]
            val previousWristXCoordinateDuringRecovery = data[4]
            val lastKneeXCoordinateDuringRecovery = data[5]
            val lastWristXCoordinateDuringRecovery = data[6]
            return analyzeData(lastElbowAngleDuringRecovery,
                previousElbowAngleDuringRecovery,
                lastKneeAngleDuringRecovery,
                previousKneeAngleDuringRecovery,
                previousWristXCoordinateDuringRecovery,
                lastKneeXCoordinateDuringRecovery,
                lastWristXCoordinateDuringRecovery)
        }
        return emptyList()
    }

    private fun extractData(normalizedFrameMeasurements: List<NormalizedFrameMeasurement>): List<Float?> {
        val firstFrameMeasurement =
            normalizedFrameMeasurements[normalizedFrameMeasurements.size - 4]
        val lastFrameMeasurement = normalizedFrameMeasurements.last()
        val lastElbowAngleDuringRecovery =
            CalculateAngles().calculateElbowAngle(lastFrameMeasurement)
        val previousElbowAngleDuringRecovery =
            CalculateAngles().calculateElbowAngle(firstFrameMeasurement)
        val lastKneeAngleDuringRecovery = CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        val previousKneeAngleDuringRecovery =
            CalculateAngles().calculateKneeAngle(firstFrameMeasurement)
        var lastKneeXCoordinateDuringRecovery: Float? = null
        var previousWristXCoordinateDuringRecovery: Float? = null
        var lastWristXCoordinateDuringRecovery: Float? = null
        for (normalizedMeasurement in firstFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                previousWristXCoordinateDuringRecovery = normalizedMeasurement.x
            }
        }
        for (normalizedMeasurement in lastFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.KNEE) {
                lastKneeXCoordinateDuringRecovery = normalizedMeasurement.x
            }
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                lastWristXCoordinateDuringRecovery = normalizedMeasurement.x
            }
        }
        return listOf(
            lastElbowAngleDuringRecovery,
            previousElbowAngleDuringRecovery,
            lastKneeAngleDuringRecovery,
            previousKneeAngleDuringRecovery,
            previousWristXCoordinateDuringRecovery,
            lastKneeXCoordinateDuringRecovery,
            lastWristXCoordinateDuringRecovery
        )
    }

    fun analyzeData(
        lastElbowAngleDuringRecovery: Float?,
        previousElbowAngleDuringRecovery: Float?,
        lastKneeAngleDuringRecovery: Float?,
        previousKneeAngleDuringRecovery: Float?,
        lastKneeXCoordinateDuringRecovery: Float?,
        previousWristXCoordinateDuringRecovery: Float?,
        lastWristXCoordinateDuringRecovery: Float?
    ): MutableList<String> {
        val feedback = mutableListOf<String>()
        if (lastWristXCoordinateDuringRecovery != null && previousWristXCoordinateDuringRecovery != null &&
            lastElbowAngleDuringRecovery != null && previousElbowAngleDuringRecovery != null) {
            if (previousWristXCoordinateDuringRecovery - lastWristXCoordinateDuringRecovery > 0.05 && previousElbowAngleDuringRecovery - lastElbowAngleDuringRecovery > 5) {
                feedback.add("Move the handle forward")
            }
        }
        if (lastKneeAngleDuringRecovery != null && 150 < lastKneeAngleDuringRecovery && lastKneeAngleDuringRecovery <= 180) {
            if (lastElbowAngleDuringRecovery != null && lastElbowAngleDuringRecovery < 150) {
                feedback.add("Straighten the arm")
            } else if (lastWristXCoordinateDuringRecovery != null && lastKneeXCoordinateDuringRecovery != null && lastWristXCoordinateDuringRecovery < lastKneeXCoordinateDuringRecovery) {
                feedback.add("Straighten arms until hands over knees")
            }
        }

        // for checking the arm first before bending knee
        if (lastElbowAngleDuringRecovery != null && lastKneeAngleDuringRecovery != null && previousElbowAngleDuringRecovery != null && previousKneeAngleDuringRecovery != null) {
            if (150 < lastKneeAngleDuringRecovery && lastKneeAngleDuringRecovery < 170) {
                if (lastElbowAngleDuringRecovery - previousElbowAngleDuringRecovery <= 5 && lastKneeAngleDuringRecovery - previousKneeAngleDuringRecovery >= 10) {
                    feedback.add("Move arms before bend your knees")
                }
            }
        }
        return feedback
    }
}