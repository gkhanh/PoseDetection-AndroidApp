package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import android.util.Log

class RowingFeedbackCounter {
    private val feedbackCounts = mutableMapOf<String, Int>()

    fun incrementFeedback(feedback: String) {
        // Log.d("RowingFeedbackCounter", "Increment feedback: $feedback")
        val count = feedbackCounts[feedback] ?: 0
        feedbackCounts[feedback] = count + 1
    }

    fun getFeedbackCounts(): Map<String, Int> {
        return feedbackCounts.filter { it.value > 0 }
    }

    fun getMostCommonFeedback(): String? {
        return feedbackCounts.filterKeys { it.isNotBlank() }.maxByOrNull { it.value }?.key
    }

    fun reset() {
        feedbackCounts.clear()
    }

}