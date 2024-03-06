package com.google.mediapipe.examples.poselandmarker.utils

import com.google.mediapipe.examples.poselandmarker.exception.EmptyDataException
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks

class CalculateAngles {
    fun calculateShoulderAngle(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float?{
        try {
            // Find the SHOULDER
            val shoulderMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.SHOULDER

            }
            // Find the ELBOW
            val elbowMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.ELBOW
            }
            // Find the HIP
            val hipMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.HIP
            }
            // Raise error if one of the measurements is missing
            if (shoulderMeasurement == null || elbowMeasurement == null || hipMeasurement == null) {
                throw EmptyDataException()
            }

            val shoulderAngle = operation.calculateAngle(
                hipMeasurement.x, hipMeasurement.y,
                shoulderMeasurement.x, shoulderMeasurement.y,
                elbowMeasurement.x, elbowMeasurement.y
            )
            return String.format("%.2f", shoulderAngle).toFloat()
        } catch (e: Exception) {
            return null
        }
    }

    fun calculateElbowAngle(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float?{
        try {
            // Find the SHOULDER
            val shoulderMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.SHOULDER

            }
            // Find the ELBOW
            val elbowMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.ELBOW
            }
            // Find the WRIST
            val wristMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.WRIST
            }
            // Raise error if one of the measurements is missing
            if (shoulderMeasurement == null || elbowMeasurement == null || wristMeasurement == null) {
                throw EmptyDataException()
            }

            val elbowAngle = operation.calculateAngle(
                wristMeasurement.x, wristMeasurement.y,
                shoulderMeasurement.x, shoulderMeasurement.y,
                elbowMeasurement.x, elbowMeasurement.y
            )
            return String.format("%.2f", elbowAngle).toFloat()
        } catch (e: Exception) {
            return null
        }
    }

    fun calculateHipAngle(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float?{
        try {
            // Find the KNEE
            val kneeMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.KNEE

            }
            // Find the HIP
            val hipMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.HIP
            }
            // Find the SHOULDER
            val shoulderMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.SHOULDER
            }
            // Raise error if one of the measurements is missing
            if (kneeMeasurement == null || hipMeasurement == null || shoulderMeasurement == null) {
                throw EmptyDataException()
            }

            val hipAngle = operation.calculateAngle(
                kneeMeasurement.x, kneeMeasurement.y,
                hipMeasurement.x, hipMeasurement.y,
                shoulderMeasurement.x, shoulderMeasurement.y
            )
            return String.format("%.2f", hipAngle).toFloat()
        } catch (e: Exception) {
            return null
        }
    }

    fun calculateKneeAngle(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float?{
        try {
            // Find the KNEE
            val kneeMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.KNEE

            }
            // Find the ANKLE
            val ankleMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.ANKLE
            }
            // Find the HIP
            val hipMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.HIP
            }
            // Raise error if one of the measurements is missing
            if (kneeMeasurement == null || ankleMeasurement == null || hipMeasurement == null) {
                throw EmptyDataException()
            }

            val kneeAngle = operation.calculateAngle(
                hipMeasurement.x, hipMeasurement.y,
                kneeMeasurement.x, kneeMeasurement.y,
                ankleMeasurement.x, ankleMeasurement.y
            )
            return String.format("%.2f", kneeAngle).toFloat()
        } catch (e: Exception) {
            return null
        }
    }

    fun calculateFootAngle(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float?{
        try {
            // Find the FOOT_INDEX
            val footIndexMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.FOOT_INDEX

            }
            // Find the HEEL
            val heelMeasurement = normalizedFrameMeasurement.normalizedMeasurements.find {
                it.landmark == NormalizedLandmarks.HEEL
            }

            // Raise error if one of the measurements is missing
            if (footIndexMeasurement == null || heelMeasurement == null) {
                throw EmptyDataException()
            }

            val footAngle = operation.calculateAngleWithXAxis(
                heelMeasurement.x, heelMeasurement.y,
                footIndexMeasurement.x, footIndexMeasurement.y
            )
            return String.format("%.2f", footAngle).toFloat()
        } catch (e: Exception) {
            return null
        }
    }

    private val operation = MathUtils()

}