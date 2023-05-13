package io.kmc.passenger.routine

import java.io.Serializable
import java.lang.Exception

fun interface SerializableFunction<T, R> : Serializable /*, Function<T, R>*/ {

    @Throws(Exception::class)
    fun apply(t: T): R
}