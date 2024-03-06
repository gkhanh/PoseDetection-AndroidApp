package com.google.mediapipe.examples.poselandmarker.utils

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class MathUtils {
    fun calculateAngle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {

        val numerator = by * (ax - cx) + ay * (cx - bx) + cy * (bx - ax)
        val denominator = (bx - ax) * (ax - cx)
        val ratio = numerator / denominator
        val angleInRad = atan(ratio)
        var angleInDeg = (angleInRad * 180) / PI

        if (angleInDeg < 0) {
            angleInDeg += 180
        }
        return angleInDeg.toFloat()
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