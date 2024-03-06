package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import android.util.Log
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable

class RowingStrokeCounter(
    private val phaseDetector: PhaseDetector,
    private val feedbackProvider: RowingFeedbackProvider
) : PhaseDetector.Listener {
    private var strokeCount = 0
    private var correctStrokeCount = 0
    private var currentPhase = Phase.OTHER
    private val listeners = mutableListOf<RowingStrokeCounterListener>()
    private var phaseDetectorCancellable: Cancellable? = null

    override fun onPhaseChange(
        currentPhase: Phase,
        frameMeasurementBuffer: List<NormalizedFrameMeasurement>
    ) {
        if (this.currentPhase == Phase.DRIVE_PHASE && currentPhase == Phase.RECOVERY_PHASE) {
            // A full stroke has been completed
            strokeCount++
            if (!feedbackProvider.wasFeedbackProvided()) {
                correctStrokeCount++
            }
            notifyListeners()
            feedbackProvider.resetFeedbackCounts()
        }
        this.currentPhase = currentPhase
    }

    fun getStrokeCount(): Int {
        return strokeCount
    }

    fun getCorrectStrokeCount(): Int {
        return correctStrokeCount
    }

    fun reset() {
        strokeCount = 0
        correctStrokeCount = 0
        currentPhase = Phase.OTHER
    }

    fun getCorrectStrokePercentage(): Int {
        return if (strokeCount > 0) {
            ((correctStrokeCount.toDouble() / strokeCount) * 100).toInt()
        } else {
            0
        }
    }

    fun addListener(listener: RowingStrokeCounterListener): Cancellable {
        listeners.add(listener)
        if (listeners.size == 1) {
            phaseDetectorCancellable = phaseDetector.addListener(this)
        }
        return Cancellable {
            listeners -= listener
            if (listeners.isEmpty()) {
                phaseDetectorCancellable?.cancel()
                phaseDetectorCancellable = null
            }
        }
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.showStrokeCount(strokeCount, getCorrectStrokePercentage())
        }
    }

    interface RowingStrokeCounterListener {
        fun showStrokeCount(strokeCount: Int, correctStrokeCountPercentage: Int)
    }
}