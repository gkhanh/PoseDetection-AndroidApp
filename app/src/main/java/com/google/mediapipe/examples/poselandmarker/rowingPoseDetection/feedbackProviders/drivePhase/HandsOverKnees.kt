package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles

class HandsOverKnees : RowingFeedbackProvider.FeedbackProvider {

    override fun getFeedback(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ): List<String> {
        if (currentPhase == Phase.RECOVERY_PHASE) {
            val data = extractData(frameMeasurementBuffer)
            val lastShoulderAngleDuringDrive = data[0]
            val lastElbowAngleDuringDrive = data[1]
            val previousWristXCoordinateDuringDrive = data[2]
            val lastKneeXCoordinateDuringDrive = data[3]
            val lastWristXCoordinateDuringDrive = data[4]
            val lastKneeAngleDuringDrive = data[5]
            return analyzeData(
                lastShoulderAngleDuringDrive,
                lastElbowAngleDuringDrive,
                previousWristXCoordinateDuringDrive,
                lastKneeXCoordinateDuringDrive,
                lastWristXCoordinateDuringDrive,
                lastKneeAngleDuringDrive
            )
        }
        return emptyList()
    }

    fun analyzeData(
        lastShoulderAngleDuringDrive: Float?,
        lastElbowAngleDuringDrive: Float?,
        previousWristXCoordinateDuringDrive: Float?,
        lastKneeXCoordinateDuringDrive: Float?,
        lastWristXCoordinateDuringDrive: Float?,
        lastKneeAngleDuringDrive: Float?
    ): List<String> {
        val feedback = mutableListOf<String>()
        if (lastShoulderAngleDuringDrive != null && lastElbowAngleDuringDrive != null
            && lastKneeXCoordinateDuringDrive != null
            && lastWristXCoordinateDuringDrive != null && previousWristXCoordinateDuringDrive != null
            && lastKneeAngleDuringDrive != null
        ) {
            if (lastKneeXCoordinateDuringDrive - 0.05 <= lastWristXCoordinateDuringDrive && lastWristXCoordinateDuringDrive < lastKneeXCoordinateDuringDrive + 0.05) {
                if (previousWristXCoordinateDuringDrive >= lastWristXCoordinateDuringDrive) {
                    feedback.add("Not pulling arm")
                }
            }
            if (!(lastShoulderAngleDuringDrive < 35) && 60 < lastElbowAngleDuringDrive && lastElbowAngleDuringDrive <= 95) {
                feedback.add("Arm not pulled back properly")
            }
            if (120 < lastKneeAngleDuringDrive && lastKneeAngleDuringDrive < 180){
                if (lastWristXCoordinateDuringDrive > lastKneeXCoordinateDuringDrive) {
                    feedback.add("Hands not over knees")
                }
            }
        }
        return feedback
    }

    private fun extractData(normalizedFrameMeasurements: List<NormalizedFrameMeasurement>): List<Float?> {
        val firstFrameMeasurement =
            normalizedFrameMeasurements[normalizedFrameMeasurements.size - 4]
        val lastFrameMeasurement = normalizedFrameMeasurements.last()
        val lastShoulderAngleDuringDrive =
            CalculateAngles().calculateShoulderAngle(lastFrameMeasurement)
        val lastElbowAngleDuringDrive = CalculateAngles().calculateElbowAngle(lastFrameMeasurement)
        var previousWristXCoordinateDuringDrive: Float? = null
        var lastKneeXCoordinateDuringDrive: Float? = null
        var lastWristXCoordinateDuringDrive: Float? = null
        val lastKneeAngleDuringDrive =
            CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        for (normalizedMeasurement in firstFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                previousWristXCoordinateDuringDrive = String.format("%.3f", normalizedMeasurement.x).toFloat()
            }
        }
        for (normalizedMeasurement in lastFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.KNEE) {
                lastKneeXCoordinateDuringDrive = String.format("%.3f", normalizedMeasurement.x).toFloat()
            }
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                lastWristXCoordinateDuringDrive = String.format("%.3f", normalizedMeasurement.x).toFloat()
            }
        }
        return listOf(
            lastShoulderAngleDuringDrive,
            lastElbowAngleDuringDrive,
            previousWristXCoordinateDuringDrive,
            lastKneeXCoordinateDuringDrive,
            lastWristXCoordinateDuringDrive,
            lastKneeAngleDuringDrive
        )
    }
}