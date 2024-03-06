package com.google.mediapipe.examples.poselandmarker.models

enum class Phase(val value: Int) {
    CATCH(1),
    DRIVE_PHASE(2),
    RELEASE(3),
    RECOVERY_PHASE(4),
    OTHER(5)
}
