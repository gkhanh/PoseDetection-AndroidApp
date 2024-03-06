package com.google.mediapipe.examples.poselandmarker.models

data class NormalizedFrameMeasurement(
    val timestampMs: Long,
    val normalizedMeasurements: List<NormalizedMeasurement>
)
