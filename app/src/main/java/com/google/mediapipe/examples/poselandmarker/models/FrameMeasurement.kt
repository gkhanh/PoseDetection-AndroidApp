package com.google.mediapipe.examples.poselandmarker.models

data class FrameMeasurement(
    val timestampMs: Long,
    val measurements: List<Measurement>
)