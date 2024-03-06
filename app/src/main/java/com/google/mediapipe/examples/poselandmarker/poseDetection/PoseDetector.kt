package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.LandmarkPosition
import com.google.mediapipe.examples.poselandmarker.models.Measurement
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class PoseDetector {
    fun process(timestampMs: Long, worldLandmarks: MutableList<MutableList<NormalizedLandmark>>) {
        val extractPoseCoordinatesFromLandmark =
            extractPoseCoordinatesFromLandmark(timestampMs, worldLandmarks)
        notifyListener(extractPoseCoordinatesFromLandmark)
    }

    private fun extractPoseCoordinatesFromLandmark(
        timestampMs: Long,
        worldLandmarks: MutableList<MutableList<NormalizedLandmark>>,
    ): FrameMeasurement {
        val positions = LandmarkPosition.values()
        val measurements = mutableListOf<Measurement>()
        worldLandmarks.forEach { landmarks ->
            positions.forEachIndexed { idx, position ->
                val landmark = landmarks[idx]
                val measurement = Measurement(
                    timestampMs,
                    position,
                    landmark.x(),
                    landmark.y(),
                    landmark.z()
                )
                measurements.add(measurement)
            }
        }
        return FrameMeasurement(timestampMs, measurements)
    }

    private var listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener): Cancellable {
        listeners += listener
        return Cancellable { listeners -= listener }
    }

    private fun notifyListener(frameMeasurement: FrameMeasurement) {
        for (listener in listeners) {
            listener.onMeasurement(frameMeasurement)
        }
    }

    interface Listener {
        fun onMeasurement(frameMeasurement: FrameMeasurement)
    }
}
