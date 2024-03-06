package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Measurement
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable

class LowpassFilterForRowingPoseDetector(private val poseDetector: PoseDetector) : PoseDetector.Listener {

    private val listeners = mutableListOf<Listener>()
    private val coefficient = 0.3F
    private var previous: FrameMeasurement? = null
    private var poseDetectorCancellable: Cancellable? = null

    override fun onMeasurement(frameMeasurement: FrameMeasurement) {
        previous = process(previous, frameMeasurement)
        notifyListeners(previous!!)
    }

    private fun process(previous: FrameMeasurement?, current: FrameMeasurement): FrameMeasurement {
        return if (previous == null) {
            current
        } else {
            checkIfDatapointExistInPrevious(previous, current)
        }
    }

    private fun checkIfDatapointExistInPrevious(previous: FrameMeasurement, current: FrameMeasurement): FrameMeasurement {
        val updatedMeasurements = current.measurements.map { measurement ->
            val previousMeasurement = findPreviousMeasurement(measurement.landmarkPosition, previous)

            if (previousMeasurement != null) {
                Measurement(
                    measurement.timestampMs,
                    measurement.landmarkPosition,
                    coefficient * measurement.x + (1 - coefficient) * previousMeasurement.x,
                    coefficient * measurement.y + (1 - coefficient) * previousMeasurement.y,
                    coefficient * measurement.z + (1 - coefficient) * previousMeasurement.z
                )
            } else {
                measurement
            }
        }

        return FrameMeasurement(current.timestampMs, updatedMeasurements)
    }

    private fun findPreviousMeasurement(landmark: Any, previous: FrameMeasurement): Measurement? {
        return previous.measurements.find { it.landmarkPosition == landmark }
    }

    private fun notifyListeners(frameMeasurement: FrameMeasurement) {
        listeners.forEach { it.onMeasurement(frameMeasurement) }
    }

    fun addListener(listener: Listener): Cancellable {
        listeners += listener
        if (listeners.size == 1) {
            poseDetectorCancellable = poseDetector.addListener(this)
        }
        return Cancellable { listeners.remove(listener) }
    }

    interface Listener {
        fun onMeasurement(frameMeasurement: FrameMeasurement)
    }
}