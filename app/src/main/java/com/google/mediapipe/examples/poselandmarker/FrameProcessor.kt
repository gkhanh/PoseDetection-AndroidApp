package com.google.mediapipe.examples.poselandmarker

import com.google.mediapipe.examples.poselandmarker.models.FrameMeasurement
import com.google.mediapipe.examples.poselandmarker.poseDetection.FrameMeasurementProvider
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.poseDetection.LowpassFilterForRowingPoseDetector
import com.google.mediapipe.examples.poselandmarker.poseDetection.NormalizedPoseDetector
import com.google.mediapipe.examples.poselandmarker.poseDetection.PoseDetector
import com.google.mediapipe.examples.poselandmarker.poseDetection.PoseDetectorSideMapping
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.IsOnRowingMachineCheck
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.PhaseDetector
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.PhaseDetectorDataProvider
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingFeedbackProvider
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.RowingStrokeCounter
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.HandsOverKnees
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.HipOpening
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.drivePhase.KneeExtension
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.ArmAndLegMovement
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.BodyPosture
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.feedbackProviders.recoveryPhase.KneeOverAnkle
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class FrameProcessor : RowingFeedbackProvider.RowingFeedbackProviderListener,
    RowingStrokeCounter.RowingStrokeCounterListener {

    private val poseDetector = PoseDetector()
    private val processedPoseDetector = LowpassFilterForRowingPoseDetector(poseDetector)

    private val normalizedPoseDetector = NormalizedPoseDetector(object : FrameMeasurementProvider {

        override fun addListener(listener: FrameMeasurementProvider.Listener): Cancellable {
            return processedPoseDetector.addListener(object : LowpassFilterForRowingPoseDetector.Listener {
                override fun onMeasurement(frameMeasurement: FrameMeasurement) {
                    return listener.onMeasurement(frameMeasurement)
                }
            })
        }
    })

    private val poseDetectorSideMapping = PoseDetectorSideMapping(object : FrameMeasurementProvider {
        override fun addListener(listener: FrameMeasurementProvider.Listener): Cancellable {
            return normalizedPoseDetector.addListener(object : NormalizedPoseDetector.Listener{
                override fun onMeasurement(frameMeasurement: FrameMeasurement) {
                    return listener.onMeasurement(frameMeasurement)
                }
            })
        }
    })

    private val isOnRowingMachineCheck = IsOnRowingMachineCheck(poseDetectorSideMapping)

    private val phaseDetector = PhaseDetector(object : PhaseDetectorDataProvider{

        override fun addIsOnRowingMachineListener(listener: (Boolean) -> Unit): Cancellable {
            return isOnRowingMachineCheck.addListener(object : IsOnRowingMachineCheck.Listener{

                override fun onRowingMachineCheck(isOnRowingMachine: Boolean) {
                    listener(isOnRowingMachine)
                }
            })
        }

        override fun addListener(listener: PhaseDetectorDataProvider.Listener): Cancellable {
            return poseDetectorSideMapping.addListener(object : PoseDetectorSideMapping.Listener{

                override fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
                    return listener.onMeasurement(normalizedFrameMeasurement)
                }
            })
        }
    })

    // Initialize feedback provider
    private val rowingFeedbackProvider = RowingFeedbackProvider(
        phaseDetector = phaseDetector,
        feedbackProviders = listOf(
            BodyPosture(),
            HandsOverKnees(),
            HipOpening(),
            KneeExtension(),
            ArmAndLegMovement(),
            KneeOverAnkle()
        )
    )

    private val rowingStrokeCounter = RowingStrokeCounter(phaseDetector, rowingFeedbackProvider)

    init {
        // Add RowingAnalyzer as a listener to RowingFeedbackProvider
        rowingFeedbackProvider.addListener(this)
        // Add RowingStrokeCounter as a listener to PhaseDetector
        phaseDetector.addListener(rowingStrokeCounter)
        // Add FrameProcessor as a listener to RowingStrokeCounter
        rowingStrokeCounter.addListener(this)
    }

    fun process(
        timestampMillis: Long,
        worldLandmarks: MutableList<MutableList<NormalizedLandmark>>
    ) {
        poseDetector.process(timestampMillis, worldLandmarks)
    }

    class Output(val message: String)

    private var feedbackListeners = mutableListOf<(Output) -> Unit>()
    private var strokeCountListeners = mutableListOf<(Output) -> Unit>()
    private var mostCommonFeedbackListeners = mutableListOf<(String) -> Unit>()

    fun addFeedbackListener(listener: (Output) -> Unit): Cancellable {
        feedbackListeners += listener
        return Cancellable {
            feedbackListeners -= listener
        }
    }

    fun addStrokeCountListener(listener: (Output) -> Unit): Cancellable {
        strokeCountListeners += listener
        return Cancellable {
            strokeCountListeners -= listener
        }
    }
    fun addMostCommonFeedbackListener(listener: (String) -> Unit): Cancellable {
        mostCommonFeedbackListeners += listener
        return Cancellable {
            mostCommonFeedbackListeners -= listener
        }
    }

    fun getCorrectStrokePercentage(): Int {
        return rowingStrokeCounter.getCorrectStrokePercentage()
    }

    private fun getMostCommonFeedback(): String? {
        return rowingFeedbackProvider.getMostCommonFeedback()
    }

    override fun onFeedback(feedback: List<Any>) {
        val nonEmptyFeedback = feedback.filter { it.toString().isNotBlank() }
        val feedbackMessage = nonEmptyFeedback.joinToString(", ")
        val outputMessage = " $feedbackMessage"
        val output = Output(outputMessage)
        for (listener in feedbackListeners) {
            listener(output)
        }
        val mostCommonFeedback = getMostCommonFeedback()
        for (listener in mostCommonFeedbackListeners) {
            listener(mostCommonFeedback ?: "No feedback provided")
        }
    }

    override fun showStrokeCount(strokeCount: Int, correctStrokeCountPercentage: Int) {
        val output = Output(" $strokeCount")
        for (listener in strokeCountListeners) {
            listener(output)
        }
    }
}