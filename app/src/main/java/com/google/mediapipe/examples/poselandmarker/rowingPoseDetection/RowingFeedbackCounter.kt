package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import android.util.Log

class RowingFeedbackCounter {
    private val feedbackCounts = mutableMapOf<String, Int>()

    fun incrementFeedback(feedback: String) {
        val count = feedbackCounts[feedback] ?: 0
        feedbackCounts[feedback] = count + 1
    }

    fun getFeedbackCounts(): Map<String, Int> {
        return feedbackCounts.filter { it.value > 0 }
    }

    fun getMostCommonFeedback(): String {
        return feedbackCounts.maxByOrNull { it.value }?.key ?: "No feedback provided"
    }

    fun reset() {
        feedbackCounts.clear()
    }

}