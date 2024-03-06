package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class HipOpening : RowingFeedbackProvider.FeedbackProvider {
    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.RECOVERY_PHASE) {
            val (previousHipAngleDuringDrive, lastHipAngleDuringDrive, lastKneeAngleDuringDrive) = extractData(
                frameMeasurementBuffer
            )
            return analyzeData(
                previousHipAngleDuringDrive,
                lastHipAngleDuringDrive,
                lastKneeAngleDuringDrive
            )
        }
        return emptyList()
    }

    fun analyzeData(
        previousHipAngleDuringDrive: Float?,
        lastHipAngleDuringDrive: Float?,
        lastKneeAngleDuringDrive: Float?
    ): List<String> {
        val feedback = mutableListOf<String>()
        if (previousHipAngleDuringDrive != null && lastHipAngleDuringDrive != null && lastKneeAngleDuringDrive != null) {
            if (lastKneeAngleDuringDrive >= 100){
                if (lastHipAngleDuringDrive <= 100 && !(previousHipAngleDuringDrive > lastHipAngleDuringDrive)) {
                    feedback.add("Hip is not open")
                }
            }
            if (lastKneeAngleDuringDrive < 100){
                if (lastHipAngleDuringDrive >= 100){
                    feedback.add("Open hip too soon")
                }
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
        val lastKneeAngleDuringDrive = CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        return listOf(
            previousHipAngleDuringDrive,
            lastHipAngleDuringDrive,
            lastKneeAngleDuringDrive
        )
    }
}