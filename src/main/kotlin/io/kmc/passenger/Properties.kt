package io.kmc.passenger

import io.kmc.passenger.routine.Passage

val VERSION = getVersion();

val ENABLE_METHOD_CACHE = true

val MAX_ATTEMPT_FETCH_METHOD = 3

fun getVersion() : String {
    return Passage::class.java.getPackage().implementationVersion ?: "[Developing]"
}