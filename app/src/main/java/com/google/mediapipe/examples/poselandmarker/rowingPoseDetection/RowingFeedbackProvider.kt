package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import android.util.Log
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable

class RowingFeedbackProvider(
    private val phaseDetector: PhaseDetector,
    private val feedbackProviders: List<FeedbackProvider>
) : PhaseDetector.Listener {

    private val listeners = mutableListOf<RowingFeedbackProviderListener>()
    private var phaseDetectorCancellable: Cancellable? = null
    private val feedbackCounter = RowingFeedbackCounter()

    fun addListener(listener: RowingFeedbackProviderListener): Cancellable {
        listeners.add(listener)
        if (listeners.size == 1) {
            phaseDetectorCancellable = phaseDetector.addListener(this)
        }
        return Cancellable { removeListener(listener) }
    }

    private fun removeListener(listener: RowingFeedbackProviderListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            phaseDetectorCancellable?.cancel()
            phaseDetectorCancellable = null
        }
    }

    private fun notifyListeners(feedback: List<Any>) {
        for (listener in listeners) {
            listener.onFeedback(feedback)
        }
    }

    override fun onPhaseChange(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ) {
        if (phaseDetector.isOnRowingMachine) {
            val feedback = feedbackProviders.flatMap { it.getFeedback(currentPhase, frameMeasurementBuffer) }
            incrementFeedbackCounts(feedback)
            for (listener in listeners) {
                listener.onFeedback(feedback)
                notifyListeners(feedback)  // Notify listeners immediately when feedback is available
            }
        }
    }

    private fun incrementFeedbackCounts(feedback: List<Any>) {
        feedback.forEach { feedbackCounter.incrementFeedback(it.toString()) }
    }

    fun resetFeedbackCounts() {
        feedbackCounter.reset()
    }

    fun getMostCommonFeedback(): String? {
        return feedbackCounter.getMostCommonFeedback()
    }

    fun wasFeedbackProvided(): Boolean {
        return feedbackCounter.getFeedbackCounts().isNotEmpty()
    }

    interface FeedbackProvider {
        fun getFeedback(
            currentPhase: Phase,
            frameMeasurementBuffer: List<NormalizedFrameMeasurement>
        ): List<String>
    }

    interface RowingFeedbackProviderListener {
        fun onFeedback(feedback: List<Any>)
    }
}