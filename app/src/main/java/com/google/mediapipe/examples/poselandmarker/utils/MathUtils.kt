package com.google.mediapipe.examples.poselandmarker.utils

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class MathUtils {
    fun calculateAngle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
        // Vector AB (from B to A)
        val ABx = ax - bx
        val ABy = ay - by

        // Vector BC (from B to C)
        val BCx = cx - bx
        val BCy = cy - by

        // Dot product of vectors AB and BC
        val dotProduct = ABx * BCx + ABy * BCy

        // Magnitude of vector AB
        val magAB = sqrt(ABx * ABx + ABy * ABy)

        // Magnitude of vector BC
        val magBC = sqrt(BCx * BCx + BCy * BCy)

        // Calculate the angle in radians between AB and BC
        val angleInRadians = acos(dotProduct / (magAB * magBC))

        // Convert to degrees
        val angleInDegrees = angleInRadians * (180.0 / Math.PI)

        return angleInDegrees.toFloat()
    }

    fun calculateAngleWithXAxis(ax: Float, ay: Float, bx: Float, by: Float): Float {
        val xDiff = bx - ax
        val yDiff = by - ay
        var angle = atan2(yDiff, xDiff) * (180.0 / PI).toFloat()
        if (angle < 0){
            angle = abs(angle)
        }
        return angle
    }

    fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}