package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import kotlin.math.max
import kotlin.math.min

interface FrameMeasurementProvider {
    fun addListener(listener: Listener): Cancellable

    interface Listener {
        fun onMeasurement(frameMeasurement: FrameMeasurement)
    }
}

data class Point(val x: Float, val y: Float)
data class Rect(var left: Float, var top: Float, var right: Float, var bottom: Float)

class NormalizedPoseDetector(private var frameMeasurementProvider: FrameMeasurementProvider) :
    FrameMeasurementProvider.Listener {
    private val listeners = mutableListOf<Listener>()
    private var frameMeasurementProviderCancellable: Cancellable? = null

    // Initialize bounding box with minimum values
    private var globalBoundingBox =
        Rect(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    override fun onMeasurement(frameMeasurement: FrameMeasurement) {
        val points = extractData(frameMeasurement)
        val boundingBox = getBoundingBox(points)
        updateGlobalBoundingBox(boundingBox)
        val normalizedPoints = normalizePose(points, globalBoundingBox)
        val normalizedFrameMeasurement = frameMeasurement.copy(
            measurements = frameMeasurement.measurements.mapIndexed { index, measurement ->
                measurement.copy(
                    x = normalizedPoints[index].x,
                    y = normalizedPoints[index].y
                )
            }
        )
        notifyListener(normalizedFrameMeasurement)
    }


    private fun extractData(frameMeasurement: FrameMeasurement): List<Point> {
        return frameMeasurement.measurements.map { Point(it.x, it.y) }
    }

    private fun getBoundingBox(pose: List<Point>): Rect {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (point in pose) {
            if (point.x < minX) minX = point.x
            if (point.y < minY) minY = point.y
            if (point.x > maxX) maxX = point.x
            if (point.y > maxY) maxY = point.y
        }

        return Rect(minX, minY, maxX, maxY)
    }

    private fun updateGlobalBoundingBox(boundingBox: Rect) {
        globalBoundingBox.left = min(globalBoundingBox.left, boundingBox.left)
        globalBoundingBox.top = min(globalBoundingBox.top, boundingBox.top)
        globalBoundingBox.right = max(globalBoundingBox.right, boundingBox.right)
        globalBoundingBox.bottom = max(globalBoundingBox.bottom, boundingBox.bottom)
    }

    private fun normalizePose(pose: List<Point>, boundingBox: Rect): List<Point> {
        val normalizedPose = mutableListOf<Point>()
        val boxWidth = boundingBox.right - boundingBox.left
        val boxHeight = boundingBox.bottom - boundingBox.top

        for (point in pose) {
            val normalizedX = (point.x - boundingBox.left) / boxWidth
            val normalizedY = (point.y - boundingBox.top) / boxHeight
            normalizedPose.add(Point(normalizedX, normalizedY))
        }
        return normalizedPose
    }

    fun addListener(listener: Listener): Cancellable {
        listeners += listener
        if (listeners.size == 1) {
            frameMeasurementProviderCancellable = frameMeasurementProvider.addListener(this)
        }
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