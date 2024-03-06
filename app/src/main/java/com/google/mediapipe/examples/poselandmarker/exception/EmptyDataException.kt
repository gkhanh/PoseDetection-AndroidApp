package com.google.mediapipe.examples.poselandmarker.exception

class EmptyDataException(message: String? = null) :
    Exception("Cannot find any or not enough data")
