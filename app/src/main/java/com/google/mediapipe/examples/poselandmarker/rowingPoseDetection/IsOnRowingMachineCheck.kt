package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import com.google.mediapipe.examples.poselandmarker.exception.EmptyDataException
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.NormalizedMeasurement
import com.google.mediapipe.examples.poselandmarker.poseDetection.PoseDetectorSideMapping
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import com.google.mediapipe.examples.poselandmarker.utils.MathUtils
import kotlin.math.abs

class IsOnRowingMachineCheck(private val poseDetectorSideMapping: PoseDetectorSideMapping) :
    PoseDetectorSideMapping.Listener {

    private val listeners = mutableListOf<Listener>()
    private var distance = 0.0F
    private var isOnRowingMachine = false
    private var poseDetectorCancellable: Cancellable? = null

    private fun calculateHeelAndHipDistance(normalizedFrameMeasurement: NormalizedFrameMeasurement): Float? {
        try {
            val distanceCalculator = MathUtils()

            // get the HEEL measurement
            var heelMeasurement: NormalizedMeasurement? = null
            if (normalizedFrameMeasurement.normalizedMeasurements.isNotEmpty()) {
                for (normalizedMeasurement in normalizedFrameMeasurement.normalizedMeasurements) {
                    if (normalizedMeasurement.landmark == NormalizedLandmarks.HEEL) {
                        heelMeasurement = normalizedMeasurement
                        break
                    }
                }
            }
            if (heelMeasurement == null) {
                return null
            }

            // get the HIP measurement
            var hipMeasurement: NormalizedMeasurement? = null
            if (normalizedFrameMeasurement.normalizedMeasurements.isNotEmpty()) {
                for (normalizedMeasurement in normalizedFrameMeasurement.normalizedMeasurements) {
                    if (normalizedMeasurement.landmark == NormalizedLandmarks.HIP) {
                        hipMeasurement = normalizedMeasurement
                        break
                    }
                }
            }
            if (hipMeasurement == null) {
                return null
            }
            distance = distanceCalculator.calculateDistance(
                heelMeasurement.x,
                heelMeasurement.y,
                hipMeasurement.x,
                hipMeasurement.y
            )
            return distance
        } catch (e: EmptyDataException) {
            return null
        }
    }

    private fun conditionCheck(normalizedFrameMeasurement: NormalizedFrameMeasurement): Boolean {

        val angleCalculator = CalculateAngles()
        val hipAngle = angleCalculator.calculateHipAngle(normalizedFrameMeasurement)
        val footAngle = angleCalculator.calculateFootAngle(normalizedFrameMeasurement)
        var kneeYCoordinate: Float? = null
        var hipYCoordinate: Float? = null
        var kneeXCoordinate: Float? = null
        var hipXCoordinate: Float? = null
        distance = calculateHeelAndHipDistance(normalizedFrameMeasurement)!!

        for (normalizedMeasurement in normalizedFrameMeasurement.normalizedMeasurements) {
            if (normalizedMeasurement.landmark == NormalizedLandmarks.KNEE) {
                kneeXCoordinate = normalizedMeasurement.x
                kneeYCoordinate = normalizedMeasurement.y
            }
            if (normalizedMeasurement.landmark == NormalizedLandmarks.HIP) {
                hipXCoordinate = normalizedMeasurement.x
                hipYCoordinate = normalizedMeasurement.y
            }
        }
        if (hipAngle != null && footAngle != null && kneeYCoordinate != null &&
            hipYCoordinate != null && kneeXCoordinate != null && hipXCoordinate != null
        ) {
            isOnRowingMachine = (hipAngle in 23.0..150.0) &&
                (abs(distance) in 0.14..0.77) &&
                (footAngle <= 70.0) &&
                (abs(kneeYCoordinate - hipYCoordinate) <= 0.34) &&
                (abs(kneeXCoordinate - hipXCoordinate) in 0.13..0.52)
        }
        //print all the values line by line
//        println("the hip angle: $hipAngle")
//        println("the foot angle: $footAngle")
//        println("distance between hip and heel: $distance")
//        println("3rd condition: ${kneeYCoordinate!! - hipYCoordinate!!}")
//        println("4th condition: ${abs(kneeXCoordinate!! - hipXCoordinate!!)}")
        return isOnRowingMachine

    }

    fun addListener(listener: Listener): Cancellable {
        listeners += listener
        if (listeners.size == 1) {
            poseDetectorCancellable = poseDetectorSideMapping.addListener(this)
        }
        return Cancellable {
            listeners -= listener
            if (listeners.isEmpty()) {
                poseDetectorCancellable?.cancel()
                poseDetectorCancellable = null
            }
        }
    }

    override fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
        isOnRowingMachine = conditionCheck(normalizedFrameMeasurement)
        // println("the person is on rowing machine: $isOnRowingMachine")
        notifyListeners()
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onRowingMachineCheck(isOnRowingMachine)
        }
    }

    interface Listener {
        fun onRowingMachineCheck(isOnRowingMachine: Boolean)
    }

}