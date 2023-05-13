package io.kmc.passenger.routine

import java.io.Serializable
import java.lang.Exception

fun interface SerializableConsumer<T> : Serializable/*, Consumer<T>*/ {

    @Throws(Exception::class)
    fun accept(t: T)
}