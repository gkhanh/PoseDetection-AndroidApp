package com.google.mediapipe.examples.poselandmarker.utils

import java.util.concurrent.locks.ReentrantLock

interface Cancellable {
    fun cancel()
    fun isCancelled(): Boolean
}

fun Cancellable(f: () -> Unit): Cancellable {
    return object : Cancellable {

        private val lock = ReentrantLock()

        private var isCancelled = false

        override fun cancel() {
            lock.lock()
            if (!isCancelled) {
                isCancelled = true
                f()
            }
            lock.unlock()
        }

        override fun isCancelled(): Boolean {
            return isCancelled
        }
    }
}