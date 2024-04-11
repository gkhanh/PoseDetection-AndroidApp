package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.MeasurementData

class RecoveryPhaseChecker {

    companion object {
        private const val THRESHOLD_X_COORDINATE = 0.015f
        private const val MIN_BUFFER_SIZE_MS = 300L
        private const val MAX_BUFFER_SIZE_MS = 8000L

        private fun formatCoordinate(value: Float): Float {
            return String.format("%.3f", value).replace(',', '.').toFloat()
        }

        fun check(data: MeasurementData, bufferSizeMs: Long): Boolean {
            if (bufferSizeMs !in MIN_BUFFER_SIZE_MS..MAX_BUFFER_SIZE_MS) return false

            data.currentKneeAngle ?: return false
            data.previousKneeAngle ?: return false
            data.currentHipAngle ?: return false
            data.previousHipAngle ?: return false

            val currentWristXCoordinate =
                data.currentWristXCoordinate?.let { formatCoordinate(it) } ?: return false
            val previousWristXCoordinate =
                data.previousWristXCoordinate?.let { formatCoordinate(it) } ?: return false
            val currentHipXCoordinate =
                data.currentHipXCoordinate?.let { formatCoordinate(it) } ?: return false
            val previousHipXCoordinate =
                data.previousHipXCoordinate?.let { formatCoordinate(it) } ?: return false
            val currentAnkleXCoordinate =
                data.currentAnkleXCoordinate?.let { formatCoordinate(it) } ?: return false
            val previousAnkleXCoordinate =
                data.previousAnkleCoordinate?.let { formatCoordinate(it) } ?: return false

            // Recovery has started when wrists are moving to the right or hips are moving closer to the ankles
            // In other words, when the difference between the current and previous x-coordinates of the wrists and hips is smaller than the threshold

            // Check if the hips and ankle and check if the wrists are moving to the left, this is when rowing technique is bad
            val currentDistanceBetweenAnkleAndHip = currentAnkleXCoordinate - currentHipXCoordinate
            val previousDistanceBetweenAnkleAndHip =
                previousAnkleXCoordinate - previousHipXCoordinate
//            println("Recovery: currentDistanceBetweenAnkleAndHip: $currentDistanceBetweenAnkleAndHip")
//            println("Recovery: previousDistanceBetweenAnkleAndHip: $previousDistanceBetweenAnkleAndHip")
//            println("Recovery: currentWristXCoordinate: $currentWristXCoordinate")
//            println("Recovery: previousWristXCoordinate: $previousWristXCoordinate")
//            if (currentDistanceBetweenAnkleAndHip < previousDistanceBetweenAnkleAndHip - THRESHOLD_X_COORDINATE && currentWristXCoordinate > previousWristXCoordinate + THRESHOLD_X_COORDINATE) {
//                println("Recovery: currentDistanceBetweenAnkleAndHip: $currentDistanceBetweenAnkleAndHip")
//                println("Recovery: previousDistanceBetweenAnkleAndHip: $previousDistanceBetweenAnkleAndHip")
//                println("Recovery: currentWristXCoordinate: $currentWristXCoordinate")
//                println("Recovery: previousWristXCoordinate: $previousWristXCoordinate")
//                return true
//            }
            return currentDistanceBetweenAnkleAndHip < previousDistanceBetweenAnkleAndHip - THRESHOLD_X_COORDINATE && currentWristXCoordinate > previousWristXCoordinate + THRESHOLD_X_COORDINATE
//            return false
        }
    }
}