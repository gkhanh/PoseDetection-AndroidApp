package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.LandmarkPosition
import com.google.mediapipe.examples.poselandmarker.models.Measurement
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import org.junit.Test

import org.junit.Assert.*


class NormalizedPoseDetectorTest {

    @Test
    fun test_basicBehavior() {
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForNormalizedPoseDetection()
        val normalizedPoseDetector = NormalizedPoseDetector(frameMeasurementProvider)

        val listener = TestListenerForNormalizedPoseDetection()
        normalizedPoseDetector.addListener(listener)

        /* When */
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(200f, 200f),
                Pair(400f, 400f),
            )
        ))

        /* Then */
        assertEquals(1, listener.result.size)
        assertEquals(0f, listener.result.first().measurements[0].x)
        assertEquals(0f, listener.result.first().measurements[0].y)
        assertEquals(1f, listener.result.first().measurements[1].x)
        assertEquals(1f, listener.result.first().measurements[1].y)
    }

    @Test
    fun test_grow() {
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForNormalizedPoseDetection()
        val normalizedPoseDetector = NormalizedPoseDetector(frameMeasurementProvider)

        val listener = TestListenerForNormalizedPoseDetection()
        normalizedPoseDetector.addListener(listener)

        /* When */
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(200f, 200f),
                Pair(400f, 400f),
            )
        ))
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(100f, 100f),
                Pair(600f, 600f),
            )
        ))

        /* Then */
        assertEquals(2, listener.result.size)
        assertEquals(0f, listener.result[1].measurements[0].x)
        assertEquals(0f, listener.result[1].measurements[0].y)
        assertEquals(1f, listener.result[1].measurements[1].x)
        assertEquals(1f, listener.result[1].measurements[1].y)
    }

    @Test
    fun test_shouldNotGrow() {
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForNormalizedPoseDetection()
        val normalizedPoseDetector = NormalizedPoseDetector(frameMeasurementProvider)

        val listener = TestListenerForNormalizedPoseDetection()
        normalizedPoseDetector.addListener(listener)

        /* When */
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(200f, 200f),
                Pair(400f, 400f),
            )
        ))
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(100f, 100f),
                Pair(600f, 600f),
            )
        ))
        frameMeasurementProvider.listener?.onMeasurement(this.measurements(
            listOf(
                Pair(200f, 200f),
                Pair(400f, 400f),
            )
        ))

        /* Then */
        assertEquals(3, listener.result.size)
        assertEquals(0.2f, listener.result[2].measurements[0].x)
        assertEquals(0.2f, listener.result[2].measurements[0].y)
        assertEquals(0.6f, listener.result[2].measurements[1].x)
        assertEquals(0.6f, listener.result[2].measurements[1].y)
    }

    private fun measurements(coordinates: List<Pair<Float, Float>>): FrameMeasurement {
        return FrameMeasurement(
            timestampMs = 0,
            measurements = coordinates.map {
                Measurement(
                    timestampMs = 0,
                    landmarkPosition = LandmarkPosition.LEFT_EAR,
                    x = it.first,
                    y = it.second,
                    z = 0f
                )
            }
        )
    }

}

class TestFrameMeasurementProviderForNormalizedPoseDetection : FrameMeasurementProvider {

    var listener: FrameMeasurementProvider.Listener? = null

    override fun addListener(listener: FrameMeasurementProvider.Listener): Cancellable {
        this.listener = listener

        return Cancellable { this.listener = null }
    }
}

class TestListenerForNormalizedPoseDetection : NormalizedPoseDetector.Listener {

    var result: MutableList<FrameMeasurement> = mutableListOf()

    override fun onMeasurement(frameMeasurement: FrameMeasurement) {
        result.add(frameMeasurement)
    }
}