package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.poseDetection.FrameMeasurementProvider
import com.google.mediapipe.examples.poselandmarker.poseDetection.PoseDetectorSideMapping
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach


class PhaseDetectorTest {

    private lateinit var isOnRowingMachineCheck: IsOnRowingMachineCheck
    private lateinit var poseDetectorSideMapping: PoseDetectorSideMapping
    private lateinit var phaseDetector: PhaseDetector

    @BeforeEach
    fun setup() {
        val frameMeasurementProvider = TestFrameMeasurementProviderForPhaseDetector()
        poseDetectorSideMapping = PoseDetectorSideMapping(frameMeasurementProvider)
        isOnRowingMachineCheck = IsOnRowingMachineCheck(poseDetectorSideMapping)

        phaseDetector = PhaseDetector(isOnRowingMachineCheck, poseDetectorSideMapping)
    }


}

class TestFrameMeasurementProviderForPhaseDetector : FrameMeasurementProvider {

    var listener: FrameMeasurementProvider.Listener? = null

    override fun addListener(listener: FrameMeasurementProvider.Listener): Cancellable {
        this.listener = listener

        return Cancellable { this.listener = null }
    }
}

class TestListenerForPhaseDetector : PhaseDetector.Listener {

    var result: MutableList<NormalizedFrameMeasurement> = mutableListOf()

    override fun onPhaseChange(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ) {
        result.addAll(frameMeasurementBuffer)
    }
}