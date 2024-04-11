package com.google.mediapipe.examples.poselandmarker.rowingPoseDetection

import com.google.mediapipe.examples.poselandmarker.models.NormalizedFrameMeasurement
import com.google.mediapipe.examples.poselandmarker.models.NormalizedLandmarks
import com.google.mediapipe.examples.poselandmarker.models.Phase
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal.DrivePhaseChecker
import com.google.mediapipe.examples.poselandmarker.rowingPoseDetection.internal.RecoveryPhaseChecker
import com.google.mediapipe.examples.poselandmarker.utils.CalculateAngles
import com.google.mediapipe.examples.poselandmarker.utils.Cancellable

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
    val previousAnkleCoordinate: Float?,
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
    private var phaseStartTimestampMs: Long? = 0L
    var isOnRowingMachine: Boolean = false

    init {
        phaseDetectorDataProvider.addIsOnRowingMachineListener {
            onRowingMachineCheck(it)
        }
    }

    private fun onRowingMachineCheck(isOnRowingMachine: Boolean) {
        this.isOnRowingMachine = isOnRowingMachine
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
            if (previousPhase != Phase.DRIVE_PHASE) {
                phaseStartTimestampMs =
                    normalizedFrameMeasurement.timestampMs  // Record the start of the phase
            }
            currentPhase = Phase.DRIVE_PHASE
        }
        if (currentPhase != Phase.RECOVERY_PHASE && this.recoveryPhaseCheck(success, data)) {
            if (previousPhase != Phase.RECOVERY_PHASE) {
                phaseStartTimestampMs =
                    normalizedFrameMeasurement.timestampMs  // Record the start of the phase
            }
            currentPhase = Phase.RECOVERY_PHASE
        }
        if (currentPhase == Phase.OTHER) {
            phaseStartTimestampMs = 0L  // Reset the start timestamp if no phase is detected
        }
        if (previousPhase != currentPhase) {
            when (currentPhase) {
                Phase.DRIVE_PHASE -> println("Started drive phase")
                Phase.RECOVERY_PHASE -> println("Started recovery phase")
                else -> println("Not a drive or recovery phase")
            }
            notifyListeners(frameMeasurementBuffer.toList())
            this.frameMeasurementBuffer = frameMeasurementBuffer.takeLast(5).toMutableList()
        }
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

    private fun drivePhaseCheck(success: Boolean, data: MeasurementData): Boolean {
        if (!success || phaseStartTimestampMs == null) {
            return false
        }
        var bufferTimeMs = frameMeasurementBuffer.last().timestampMs - phaseStartTimestampMs!!
        if (bufferTimeMs > 8000) {
            bufferTimeMs = 0
            phaseStartTimestampMs = frameMeasurementBuffer.last().timestampMs
        }
        return DrivePhaseChecker.check(data, bufferTimeMs)
    }

    private fun recoveryPhaseCheck(success: Boolean, data: MeasurementData): Boolean {
        if (!success || phaseStartTimestampMs == null) {
            return false
        }
        var bufferTimeMs = frameMeasurementBuffer.last().timestampMs - phaseStartTimestampMs!!
        if (bufferTimeMs > 8000) {
            bufferTimeMs = 0
            phaseStartTimestampMs = frameMeasurementBuffer.last().timestampMs
        }
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
        var previousAnkleCoordinate: Float? = null

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
                NormalizedLandmarks.ANKLE -> previousAnkleCoordinate = normalizedMeasurement.x
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
                previousAnkleCoordinate,
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
