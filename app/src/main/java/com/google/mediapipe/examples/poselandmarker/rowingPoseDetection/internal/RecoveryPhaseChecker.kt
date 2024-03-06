package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal

import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.MeasurementData

class RecoveryPhaseChecker {

    companion object {
        private var thresholdXCoordinate = 0.05
        private var thresholdAngle = 5
        fun check(data: MeasurementData, bufferSizeMs: Long):Boolean {
            val currentKneeAngle = data.currentKneeAngle
            val previousKneeAngle = data.previousKneeAngle
            val currentHipAngle = data.currentHipAngle
            val previousHipAngle = data.previousHipAngle
            val currentWristXCoordinate = String.format("%.3f", data.currentWristXCoordinate).toFloat()
            val previousWristXCoordinate =
                String.format("%.3f", data.previousWristXCoordinate).toFloat()
            val currentHipXCoordinate = String.format("%.3f", data.currentHipXCoordinate).toFloat()
            val previousHipXCoordinate = String.format("%.3f", data.previousHipXCoordinate).toFloat()

            if (bufferSizeMs in 200..3000) {
                if (currentKneeAngle != null && previousKneeAngle != null &&
                    currentHipAngle != null && previousHipAngle != null
                ) {
                    if (currentWristXCoordinate > previousWristXCoordinate + thresholdXCoordinate &&
                        currentHipXCoordinate > previousHipXCoordinate + thresholdXCoordinate &&
                        currentKneeAngle + thresholdAngle < previousKneeAngle
                    ) {
                        return true
                    }
                }
            }
            return false
        }
    }
}