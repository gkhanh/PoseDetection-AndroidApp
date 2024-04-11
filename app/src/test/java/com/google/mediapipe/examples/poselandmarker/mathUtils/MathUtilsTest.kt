package com.google.mediapipe.examples.poselandmarker.mathUtils

import com.google.mediapipe.examples.poselandmarker.utils.MathUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MathUtilsTest {

    @Test
    fun calculateAngleTest() {
        // Given
        val mathUtilsObject = MathUtils()
        val angle = mathUtilsObject.calculateAngle(1f, 2f, 2f, 4f, 3f, 6f)
        val angle2 = mathUtilsObject.calculateAngle(1f, 2f, 3f, 2f, 3f, 4f)
        // When


        // Then
        assertEquals(180f, angle, 0.01f)
        assertEquals(90f, angle2, 0.01f)}
}