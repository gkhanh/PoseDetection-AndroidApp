package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.LandmarkPosition
import com.google.mediapipe.examples.poselandmarker.models.Measurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.NormalizedMeasurement
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import java.util.concurrent.CopyOnWriteArrayList

class PoseDetectorSideMapping(private val frameMeasurementProvider: FrameMeasurementProvider) :
    FrameMeasurementProvider.Listener {

    private var frameMeasurementProviderCancellable: Cancellable? = null
    private val listeners = CopyOnWriteArrayList<Listener>()
    // private var listeners = listOf<Listener>()
    private fun extractData(frameMeasurement: FrameMeasurement?): List<Float?> {
        var leftFootXCoordinate: Float? = null
        var rightFootXCoordinate: Float? = null
        var leftHipXCoordinate: Float? = null
        var rightHipXCoordinate: Float? = null

        frameMeasurement?.let {
            for (measurement in it.measurements) {
                when (measurement.landmarkPosition) {
                    LandmarkPosition.LEFT_FOOT_INDEX -> leftFootXCoordinate = measurement.x
                    LandmarkPosition.RIGHT_FOOT_INDEX -> rightFootXCoordinate = measurement.x
                    LandmarkPosition.LEFT_HIP -> leftHipXCoordinate = measurement.x
                    LandmarkPosition.RIGHT_HIP -> rightHipXCoordinate = measurement.x
                    else -> {

                    }
                }
            }
        }
        return listOf(
            leftFootXCoordinate,
            rightFootXCoordinate,
            leftHipXCoordinate,
            rightHipXCoordinate
        )
    }

    private fun reverseMeasurement(frameMeasurement: FrameMeasurement): FrameMeasurement {
        val reversedMeasurements = mutableListOf<Measurement>()
        frameMeasurement.let {
            for (measurement in it.measurements) {
                // Reverse x-coordinate (assuming the range is from 0 to 1)
                val reversedMeasurement = Measurement(
                    measurement.timestampMs,
                    measurement.landmarkPosition,
                    1 - measurement.x,  // Reverse the x-coordinate
                    measurement.y,
                    measurement.z
                )
                reversedMeasurements.add(reversedMeasurement)
            }
        }
        return FrameMeasurement(frameMeasurement.timestampMs, reversedMeasurements)
    }

    fun isOnRightSide(frameMeasurement: FrameMeasurement?): Boolean {
        val data = extractData(frameMeasurement)
        val leftFootXCoordinate: Float? = data[0]
        val rightFootXCoordinate: Float? = data[1]
        val leftHipXCoordinate: Float? = data[2]
        val rightHipXCoordinate: Float? = data[3]
        if (leftFootXCoordinate != null && rightFootXCoordinate != null && leftHipXCoordinate != null && rightHipXCoordinate != null) {
            return (leftFootXCoordinate > leftHipXCoordinate || rightFootXCoordinate > rightHipXCoordinate)
        }
        return false
    }

    fun isOnLeftSide(frameMeasurement: FrameMeasurement?): Boolean {
        val data = extractData(frameMeasurement)
        val leftFootXCoordinate: Float? = data[0]
        val rightFootXCoordinate: Float? = data[1]
        val leftHipXCoordinate: Float? = data[2]
        val rightHipXCoordinate: Float? = data[3]
        if (leftFootXCoordinate != null && rightFootXCoordinate != null && leftHipXCoordinate != null && rightHipXCoordinate != null) {
            return (leftFootXCoordinate < leftHipXCoordinate || rightFootXCoordinate < rightHipXCoordinate)
        }
        return false
    }

    private fun mapToRight(frameMeasurement: FrameMeasurement): NormalizedFrameMeasurement {
        val normalizedMeasurements = mutableListOf<NormalizedMeasurement>()
        frameMeasurement.let {
            for (measurement in it.measurements) {
                if (measurement.landmarkPosition.name.startsWith("RIGHT_")) {
                    val newLandmark = measurement.landmarkPosition.name.split("RIGHT_").last()
                    try {
                        val normalizedLandmarks = NormalizedLandmarks.valueOf(newLandmark)
                        val normalizedMeasurement = NormalizedMeasurement(
                            timestampMs = measurement.timestampMs,
                            landmark = normalizedLandmarks,
                            x = measurement.x,
                            y = measurement.y,
                            z = measurement.z
                        )
                        normalizedMeasurements.add(normalizedMeasurement)
                    } catch (e: IllegalAccessException) {
                        println("Invalid Landmark!")
                    }
                }
            }
        }
        return NormalizedFrameMeasurement(frameMeasurement.timestampMs, normalizedMeasurements)
    }

    private fun mapToLeft(frameMeasurement: FrameMeasurement): NormalizedFrameMeasurement {
        val normalizedMeasurements = mutableListOf<NormalizedMeasurement>()
        frameMeasurement.let {
            for (measurement in it.measurements) {
                if (measurement.landmarkPosition.name.startsWith("LEFT_")) {
                    val newLandmark = measurement.landmarkPosition.name.split("LEFT_").last()
                    try {
                        val normalizedLandmarks = NormalizedLandmarks.valueOf(newLandmark)
                        val normalizedMeasurement = NormalizedMeasurement(
                            timestampMs = measurement.timestampMs,
                            landmark = normalizedLandmarks,
                            x = measurement.x,
                            y = measurement.y,
                            z = measurement.z
                        )
                        normalizedMeasurements.add(normalizedMeasurement)
                    } catch (e: IllegalAccessException) {
                        println("Invalid Landmark!")
                    }
                }
            }
        }
        return NormalizedFrameMeasurement(frameMeasurement.timestampMs, normalizedMeasurements)
    }

    fun addListener(listener: Listener): Cancellable {
        listeners += listener
        if (listeners.size == 1) {
            frameMeasurementProviderCancellable = frameMeasurementProvider.addListener(this)
        }
        return Cancellable { listeners -= listener }
    }

    override fun onMeasurement(frameMeasurement: FrameMeasurement) {
        if (isOnRightSide(frameMeasurement)) {
            notifyListener(mapToRight(frameMeasurement))
        } else if (isOnLeftSide(frameMeasurement)) {
            val reversedMeasurement = reverseMeasurement(frameMeasurement)
            notifyListener(mapToLeft(reversedMeasurement))
        }
    }

    private fun notifyListener(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
        for (listener in listeners) {
            listener.onMeasurement(normalizedFrameMeasurement)
        }
    }

    interface Listener {
        fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement)
    }
}

