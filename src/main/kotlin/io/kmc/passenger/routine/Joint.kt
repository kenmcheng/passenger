package io.kmc.passenger.routine

import java.io.Serializable

interface Joint<T> : Serializable {

    fun steer(): Passage<T>
}