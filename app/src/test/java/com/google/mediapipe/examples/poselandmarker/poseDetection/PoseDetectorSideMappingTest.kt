package com.google.mediapipe.examples.poselandmarker.poseDetection

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.LandmarkPosition
import com.google.mediapipe.examples.poselandmarker.models.Measurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import org.junit.Assert.*
import org.junit.Test

class PoseDetectorSideMappingTest {

    @Test
    fun test_basicBehavior() {
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForPoseDetectorSideMapping()
        val rowingPoseDetector = PoseDetectorSideMapping(frameMeasurementProvider)

        val listener = TestListenerForPoseDetectorSideMapping()
        rowingPoseDetector.addListener(listener)

        /* When */
        frameMeasurementProvider.listener?.onMeasurement(this.measurements())

        /* Then */
        // Check if the measurements were correctly processed
        // Check if the measurements were correctly processed and mapped into normalized landmarks
        assertEquals(1, listener.result.size)
        val firstResult = listener.result.first()
        val leftFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        val rightFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        assertNotNull(leftFootMeasurement)
        assertNotNull(rightFootMeasurement)
        assertEquals(0.3f, leftFootMeasurement?.x)
        assertEquals(0.6f, rightFootMeasurement?.x)
    }

    @Test
    fun test_isOnLeftSide(){
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForPoseDetectorSideMapping()
        val rowingPoseDetector = PoseDetectorSideMapping(frameMeasurementProvider)
        val listener = TestListenerForPoseDetectorSideMapping()
        rowingPoseDetector.addListener(listener)

        /* When */
        val isOnLeftSide = rowingPoseDetector.isOnLeftSide(measurements())
        frameMeasurementProvider.listener?.onMeasurement(this.measurements())

        /* Then */
        assertEquals(1, listener.result.size)
        val firstResult = listener.result.first()
        val leftFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        val rightFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        assertNotNull(leftFootMeasurement)
        assertNotNull(rightFootMeasurement)
        assertEquals(0.9f, leftFootMeasurement?.x)
        assertEquals(0.9f, rightFootMeasurement?.x)
        assertTrue(isOnLeftSide)
    }

    @Test
    fun test_isOnRightSide(){
        /* Given */
        val frameMeasurementProvider = TestFrameMeasurementProviderForPoseDetectorSideMapping()
        val rowingPoseDetector = PoseDetectorSideMapping(frameMeasurementProvider)
        val listener = TestListenerForPoseDetectorSideMapping()
        rowingPoseDetector.addListener(listener)

        /* When */
        val isOnRightSide = rowingPoseDetector.isOnRightSide(measurements())
        frameMeasurementProvider.listener?.onMeasurement(this.measurements())

        /* Then */
        assertEquals(1, listener.result.size)
        val firstResult = listener.result.first()
        val leftFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        val rightFootMeasurement = firstResult.normalizedMeasurements.find { it.landmark == NormalizedLandmarks.FOOT_INDEX }
        assertNotNull(leftFootMeasurement)
        assertNotNull(rightFootMeasurement)
        assertEquals(0.6f, leftFootMeasurement?.x)
        assertEquals(0.6f, rightFootMeasurement?.x)
        assertTrue(isOnRightSide)
    }

    private fun measurements(): FrameMeasurement {
        return FrameMeasurement(
            timestampMs = 0,
            measurements = listOf(
                Measurement(
                    timestampMs = 0,
                    landmarkPosition = LandmarkPosition.LEFT_FOOT_INDEX,
                    x = 0.1f,
                    y = 0.6f,
                    z = 0f
                ),
                Measurement(
                    timestampMs = 0,
                    landmarkPosition = LandmarkPosition.RIGHT_FOOT_INDEX,
                    x = 0.13f,
                    y = 0.2f,
                    z = 0f
                ),
                Measurement(
                    timestampMs = 0,
                    landmarkPosition = LandmarkPosition.LEFT_HIP,
                    x = 0.7f,
                    y = 0.5f,
                    z = 0f
                ),
                Measurement(
                    timestampMs = 0,
                    landmarkPosition = LandmarkPosition.RIGHT_HIP,
                    x = 0.72f,
                    y = 0.4f,
                    z = 0f
                )
            )
        )
    }
}

class TestFrameMeasurementProviderForPoseDetectorSideMapping : FrameMeasurementProvider {

    var listener: FrameMeasurementProvider.Listener? = null

    override fun addListener(listener: FrameMeasurementProvider.Listener): Cancellable {
        this.listener = listener

        return Cancellable { this.listener = null }
    }
}

class TestListenerForPoseDetectorSideMapping : PoseDetectorSideMapping.Listener {

    var result: MutableList<NormalizedFrameMeasurement> = mutableListOf()

    override fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
        result.add(normalizedFrameMeasurement)
    }
}

