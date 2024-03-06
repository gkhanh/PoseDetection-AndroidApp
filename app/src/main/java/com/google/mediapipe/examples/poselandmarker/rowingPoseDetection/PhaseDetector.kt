package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal.DrivePhaseChecker
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal.RecoveryPhaseChecker
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable
import java.util.concurrent.CopyOnWriteArrayList

data class MeasurementData(
    val currentElbowAngle: Float?,
    val previousElbowAngle: Float?,
    val currentKneeAngle: Float?,
    val previousKneeAngle: Float?,
    val currentHipAngle: Float?,
    val previousHipAngle: Float?,
    val currentShoulderAngle: Float?,
    val currentKneeXCoordinate: Float?,
    val currentAnkleXCoordinate: Float?,
    val currentWristXCoordinate: Float?,
    val previousWristXCoordinate: Float?,
    val currentHipXCoordinate: Float?,
    val previousHipXCoordinate: Float?
)

interface PhaseDetectorDataProvider {

    fun addIsOnRowingMachineListener(listener: (Boolean) -> Unit): Cancellable

    fun addListener(listener: Listener): Cancellable


    interface Listener {
        fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement)
    }
}

class PhaseDetector(
    private val phaseDetectorDataProvider: PhaseDetectorDataProvider
) : PhaseDetectorDataProvider.Listener {

    private var isOnRowingMachineCheckCancellable: Cancellable? = null
    private var poseDetectorCancellable: Cancellable? = null
    private val listeners = mutableListOf<Listener>()
    private var frameMeasurementBuffer = mutableListOf<NormalizedFrameMeasurement>()
    private var currentPhase = Phase.OTHER

    init {
        phaseDetectorDataProvider.addIsOnRowingMachineListener {
            onRowingMachineCheck(it)
        }
    }

    private fun onRowingMachineCheck(isOnRowingMachine: Boolean) {
        if (isOnRowingMachine) {
            if (poseDetectorCancellable == null) {
                poseDetectorCancellable = phaseDetectorDataProvider.addListener(this)
            }
        } else {
            poseDetectorCancellable?.cancel()
            poseDetectorCancellable = null
        }
    }

    override fun onMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
        val previousPhase = currentPhase
        collectFrameMeasurement(normalizedFrameMeasurement)
        val (success, data) = extractData()  // Call extractData here
        if (currentPhase != Phase.DRIVE_PHASE && this.drivePhaseCheck(success, data)) {
            currentPhase = Phase.DRIVE_PHASE
        }
        if (currentPhase != Phase.RECOVERY_PHASE && this.recoveryPhaseCheck(success, data)) {
            currentPhase = Phase.RECOVERY_PHASE
        }
        if (previousPhase != currentPhase) {
            when (currentPhase) {
                Phase.DRIVE_PHASE -> println("Started a new drive")
                Phase.RECOVERY_PHASE -> println("Started a new recovery")
                else -> println("Ended a drive or recovery")
            }
            notifyListeners(frameMeasurementBuffer.toList())
            this.frameMeasurementBuffer = frameMeasurementBuffer.takeLast(5).toMutableList()
            // frameMeasurementBuffer.drop(5).toMutableList()
        }
        Log.d("PhaseDetector", "Current phase: $currentPhase")
    }

    private fun collectFrameMeasurement(normalizedFrameMeasurement: NormalizedFrameMeasurement) {
        frameMeasurementBuffer.add(normalizedFrameMeasurement)
    }

    fun addListener(listener: Listener): Cancellable {
        listeners.add(listener)
        if (listeners.size == 1) {
            isOnRowingMachineCheckCancellable = phaseDetectorDataProvider.addListener(this)
        }
        return Cancellable { removeListener(listener) }
    }

    private fun removeListener(listener: Listener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            isOnRowingMachineCheckCancellable?.cancel()
            isOnRowingMachineCheckCancellable = null
        }
    }
    private fun notifyListeners(frameMeasurementBuffer: List<NormalizedFrameMeasurement>) {
        for (listener in listeners) {
            listener.onPhaseChange(currentPhase, frameMeasurementBuffer)
        }
    }

    fun drivePhaseCheck(success: Boolean, data: MeasurementData): Boolean {
        if (!success) {
            return false
        }

        val bufferTimeMs = frameMeasurementBuffer.last().timestampMs - frameMeasurementBuffer[frameMeasurementBuffer.size - 5].timestampMs

        return DrivePhaseChecker.check(data, bufferTimeMs)
    }

    fun recoveryPhaseCheck(success: Boolean, data: MeasurementData): Boolean {
        if (!success) {
            return false
        }

        val bufferTimeMs =
            frameMeasurementBuffer.last().timestampMs - frameMeasurementBuffer[frameMeasurementBuffer.size - 5].timestampMs

        return RecoveryPhaseChecker.check(data, bufferTimeMs)
    }

    private fun extractData(): Pair<Boolean, MeasurementData> {
        if (frameMeasurementBuffer.size < 5) {
            return Pair(
                false,
                MeasurementData(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        }
        val firstFrameMeasurement = frameMeasurementBuffer[frameMeasurementBuffer.size - 5]
        val lastFrameMeasurement = frameMeasurementBuffer.last()

        val currentElbowAngle = CalculateAngles().calculateElbowAngle(lastFrameMeasurement)
        val previousElbowAngle = CalculateAngles().calculateElbowAngle(firstFrameMeasurement)

        val currentKneeAngle = CalculateAngles().calculateKneeAngle(lastFrameMeasurement)
        val previousKneeAngle = CalculateAngles().calculateKneeAngle(firstFrameMeasurement)

        val currentHipAngle = CalculateAngles().calculateHipAngle(lastFrameMeasurement)
        val previousHipAngle = CalculateAngles().calculateHipAngle(firstFrameMeasurement)

        val currentShoulderAngle = CalculateAngles().calculateShoulderAngle(lastFrameMeasurement)

        var currentKneeXCoordinate: Float? = null
        var currentAnkleXCoordinate: Float? = null

        var currentWristXCoordinate: Float? = null
        var previousWristXCoordinate: Float? = null

        var currentHipXCoordinate: Float? = null
        var previousHipXCoordinate: Float? = null

        for (normalizedMeasurement in lastFrameMeasurement.normalizedMeasurements) {
            when (normalizedMeasurement.landmark) {
                NormalizedLandmarks.KNEE -> currentKneeXCoordinate = normalizedMeasurement.x
                NormalizedLandmarks.ANKLE -> currentAnkleXCoordinate = normalizedMeasurement.x
                NormalizedLandmarks.WRIST -> currentWristXCoordinate = normalizedMeasurement.x
                NormalizedLandmarks.HIP -> currentHipXCoordinate = normalizedMeasurement.x
                else -> {
                    continue
                }
            }
        }
        for (normalizedMeasurement in firstFrameMeasurement.normalizedMeasurements) {
            when (normalizedMeasurement.landmark) {
                NormalizedLandmarks.WRIST -> previousWristXCoordinate = normalizedMeasurement.x
                NormalizedLandmarks.HIP -> previousHipXCoordinate = normalizedMeasurement.x
                else -> {
                    continue
                }
            }
        }

        return Pair(
            true, MeasurementData(
                currentElbowAngle,
                previousElbowAngle,
                currentKneeAngle,
                previousKneeAngle,
                currentHipAngle,
                previousHipAngle,
                currentShoulderAngle,
                currentKneeXCoordinate,
                currentAnkleXCoordinate,
                currentWristXCoordinate,
                previousWristXCoordinate,
                currentHipXCoordinate,
                previousHipXCoordinate
            )
        )
    }

    interface Listener {
        fun onPhaseChange(
            currentPhase: Phase,
            frameMeasurementBuffer: List<NormalizedFrameMeasurement>
        )
    }
}
