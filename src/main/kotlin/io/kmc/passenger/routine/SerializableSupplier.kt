package io.kmc.passenger.routine

import java.io.Serializable
import java.lang.Exception

fun interface SerializableSupplier<T> : Serializable/*, Supplier<T>*/ {

    @Throws(Exception::class)
    fun get(): T
}