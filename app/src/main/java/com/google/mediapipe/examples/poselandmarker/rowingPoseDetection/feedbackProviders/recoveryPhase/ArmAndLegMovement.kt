package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles
import kotlin.math.abs

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
            return analyzeData(
                lastElbowAngleDuringRecovery,
                previousElbowAngleDuringRecovery,
                lastKneeAngleDuringRecovery,
                previousKneeAngleDuringRecovery,
                previousWristXCoordinateDuringRecovery,
                lastKneeXCoordinateDuringRecovery,
                lastWristXCoordinateDuringRecovery,
            )
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
        var previousWristYCoordinateDuringRecovery: Float? = null
        var lastWristYCoordinateDuringRecovery: Float? = null
        for (normalizedMeasurement in firstFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                previousWristXCoordinateDuringRecovery = normalizedMeasurement.x
                previousWristYCoordinateDuringRecovery = normalizedMeasurement.y
            }
        }
        for (normalizedMeasurement in lastFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.KNEE) {
                lastKneeXCoordinateDuringRecovery = normalizedMeasurement.x
            }
            if (normalizedMeasurement.landmark == NormalizedLandmarks.WRIST) {
                lastWristXCoordinateDuringRecovery = normalizedMeasurement.x
                lastWristYCoordinateDuringRecovery = normalizedMeasurement.y
            }
        }
        return listOf(
            lastElbowAngleDuringRecovery,
            previousElbowAngleDuringRecovery,
            lastKneeAngleDuringRecovery,
            previousKneeAngleDuringRecovery,
            previousWristXCoordinateDuringRecovery,
            lastKneeXCoordinateDuringRecovery,
            lastWristXCoordinateDuringRecovery,
            lastWristYCoordinateDuringRecovery,
            previousWristYCoordinateDuringRecovery,
        )
    }

    fun analyzeData(
        lastElbowAngleDuringRecovery: Float?,
        previousElbowAngleDuringRecovery: Float?,
        lastKneeAngleDuringRecovery: Float?,
        previousKneeAngleDuringRecovery: Float?,
        lastKneeXCoordinateDuringRecovery: Float?,
        previousWristXCoordinateDuringRecovery: Float?,
        lastWristXCoordinateDuringRecovery: Float?,
    ): MutableList<String> {
        val feedback = mutableListOf<String>()

        if (lastWristXCoordinateDuringRecovery != null && previousWristXCoordinateDuringRecovery != null &&
            lastElbowAngleDuringRecovery != null && previousElbowAngleDuringRecovery != null &&
            lastKneeAngleDuringRecovery != null && previousKneeAngleDuringRecovery != null &&
            lastKneeXCoordinateDuringRecovery != null) {
            val wristCoordinateChange =
                lastWristXCoordinateDuringRecovery - previousWristXCoordinateDuringRecovery
            val kneeAngleChange = previousKneeAngleDuringRecovery - lastKneeAngleDuringRecovery

            // Check if arms are extending forward
            if (wristCoordinateChange < WRIST_COORDINATE_CHANGE_THRESHOLD) {
                feedback.add("Extend your arms forward during recovery.")
            }

            // Check if knees are bending too early
            if (abs(kneeAngleChange) >= KNEE_ANGLE_CHANGE_THRESHOLD) {
                if (lastKneeAngleDuringRecovery <= KNEE_ANGLE_UPPER_LIMIT) {
                    if (lastWristXCoordinateDuringRecovery < lastKneeXCoordinateDuringRecovery - X_THRESHOLD) {
                        feedback.add("Avoid bending your knees too early. Wait until your arms are extended forward.")
                    }
                }
            }

            if (lastElbowAngleDuringRecovery < ELBOW_ANGLE_FULL_EXTENSION_THRESHOLD) {
                feedback.add("Fully extend your arms during recovery.")
            }

            // Check if arms are not extending forward and knees are bending
            if (wristCoordinateChange <= WRIST_COORDINATE_CHANGE_THRESHOLD && kneeAngleChange >= KNEE_ANGLE_CHANGE_THRESHOLD) {
                feedback.add("Focus on extending your arms forward before bending your knees during recovery.")
            }
        }
        return feedback
    }

    companion object Thresholds {
        const val WRIST_COORDINATE_CHANGE_THRESHOLD = 0.02f
        const val X_THRESHOLD = 0.02f
        const val KNEE_ANGLE_UPPER_LIMIT = 40f
        const val KNEE_ANGLE_CHANGE_THRESHOLD = 4f
        const val ELBOW_ANGLE_FULL_EXTENSION_THRESHOLD = 145f
    }
}