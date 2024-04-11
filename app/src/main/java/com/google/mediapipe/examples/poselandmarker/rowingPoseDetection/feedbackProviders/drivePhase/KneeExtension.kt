package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class KneeExtension : RowingFeedbackProvider.FeedbackProvider {
    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.RECOVERY_PHASE) {
            val (previousHipAngleDuringDrive, lastHipAngleDuringDrive, previousKneeAngleDuringDrive, lastKneeAngleDuringDrive) = extractData(
                frameMeasurementBuffer
            )
            return analyzeData(
                previousHipAngleDuringDrive,
                lastHipAngleDuringDrive,
                previousKneeAngleDuringDrive,
                lastKneeAngleDuringDrive
            )
        }
        return emptyList()
    }

    fun analyzeData(
        previousHipAngleDuringDrive: Float?,
        lastHipAngleDuringDrive: Float?,
        previousKneeAngleDuringDrive: Float?,
        lastKneeAngleDuringDrive: Float?
    ): List<String> {
        val feedback = mutableListOf<String>()
        if (previousKneeAngleDuringDrive != null && lastKneeAngleDuringDrive != null && lastKneeAngleDuringDrive > previousKneeAngleDuringDrive
            && previousHipAngleDuringDrive != null && lastHipAngleDuringDrive != null
        ) {
            if (previousKneeAngleDuringDrive >= 160 && lastHipAngleDuringDrive in 60.0F..previousHipAngleDuringDrive && previousHipAngleDuringDrive <= 90) {
                feedback.add("Keep your back straight when extending legs")
            }
            if (lastKneeAngleDuringDrive < KNEE_EXTENSION_ANGLE_THRESHOLD) {
                feedback.add("Legs not fully extended")
            }

        }
        return feedback
    }

    private fun extractData(normalizedFrameMeasurements: List<NormalizedFrameMeasurement>): List<Float?> {
        val firstFrameMeasurement =
            normalizedFrameMeasurements[normalizedFrameMeasurements.size - 4]
        val lastFrameMeasurement = normalizedFrameMeasurements.last()
        val previousHipAngleDuringDrive = CalculateAngles().calculateHipAngle(firstFrameMeasurement)
        val lastHipAngleDuringDrive = CalculateAngles().calculateElbowAngle(lastFrameMeasurement)
        val previousKneeAngleDuringDrive =
            CalculateAngles().calculateKneeAngle(firstFrameMeasurement)
        val lastKneeAngleDuringDrive = CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        return listOf(
            previousHipAngleDuringDrive,
            lastHipAngleDuringDrive,
            previousKneeAngleDuringDrive,
            lastKneeAngleDuringDrive
        )
    }

    companion object Thresholds {
        const val KNEE_EXTENSION_ANGLE_THRESHOLD = 150
    }
}