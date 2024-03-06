package com.google.mediapipe.examples.poselandmarker.models

enum class NormalizedLandmarks {
    SHOULDER, ELBOW, WRIST, PINKY, INDEX, THUMB, HIP, KNEE, ANKLE, HEEL, FOOT_INDEX, EYE, EAR,
    EYE_INNER, EYE_OUTER
}

data class NormalizedMeasurement(
    val timestampMs: Long,
    val landmark: NormalizedLandmarks,
    val x: Float,
    val y: Float,
    val z: Float
)