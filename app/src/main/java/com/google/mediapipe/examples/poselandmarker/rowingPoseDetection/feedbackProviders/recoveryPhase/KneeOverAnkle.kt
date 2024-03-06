package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class KneeOverAnkle : RowingFeedbackProvider.FeedbackProvider {

    private fun extractData(normalizedFrameMeasurements: List<NormalizedFrameMeasurement>): List<Float?> {
        val firstFrameMeasurement =
            normalizedFrameMeasurements[normalizedFrameMeasurements.size - 4]
        val lastFrameMeasurement = normalizedFrameMeasurements.last()
        val previousKneeAngleDuringRecovery =
            CalculateAngles().calculateKneeAngle(firstFrameMeasurement)
        val lastKneeAngleDuringRecovery = CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        var lastKneeXCoordinateDuringRecovery: Float? = null
        var lastAnkleXCoordinateDuringRecovery: Float? = null

        for (normalizedMeasurement in lastFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.KNEE) {
                lastKneeXCoordinateDuringRecovery = normalizedMeasurement.x
            }
            if (normalizedMeasurement.landmark == NormalizedLandmarks.ANKLE) {
                lastAnkleXCoordinateDuringRecovery = normalizedMeasurement.x
            }
        }

        return listOf(
            previousKneeAngleDuringRecovery,
            lastKneeAngleDuringRecovery,
            lastKneeXCoordinateDuringRecovery,
            lastAnkleXCoordinateDuringRecovery
        )
    }

    private fun analyzeData(
        previousKneeAngleDuringRecovery: Float?,
        lastKneeAngleDuringRecovery: Float?,
        lastKneeXCoordinateDuringRecovery: Float?,
        lastAnkleXCoordinateDuringRecovery: Float?
    ): MutableList<String> {
        val feedback = mutableListOf<String>()
        if (previousKneeAngleDuringRecovery != null && lastKneeAngleDuringRecovery != null && lastKneeAngleDuringRecovery < previousKneeAngleDuringRecovery &&
            lastKneeXCoordinateDuringRecovery != null && lastAnkleXCoordinateDuringRecovery != null && !(lastAnkleXCoordinateDuringRecovery - 0.15 < lastKneeXCoordinateDuringRecovery && lastKneeXCoordinateDuringRecovery < lastAnkleXCoordinateDuringRecovery + 0.15)
        ) {
            feedback.add("Knee must align with ankle")
        }
        return feedback
    }

    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.DRIVE_PHASE) {
            val (previousKneeAngleDuringRecovery, lastKneeAngleDuringRecovery, lastKneeXCoordinateDuringRecovery, lastAnkleXCoordinateDuringRecovery) = extractData(
                frameMeasurementBuffer
            )
            // println("prevKneeAngle: $previousKneeAngleDuringRecovery, lastKneeAngle: $lastKneeAngleDuringRecovery, KneeXCoord: $lastKneeXCoordinateDuringRecovery, AnkleXCoord: $lastAnkleXCoordinateDuringRecovery")
            return analyzeData(
                previousKneeAngleDuringRecovery,
                lastKneeAngleDuringRecovery,
                lastKneeXCoordinateDuringRecovery,
                lastAnkleXCoordinateDuringRecovery
            )
        }

        return emptyList()
    }
}
