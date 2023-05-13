package io.kmc.passenger.routine

import java.io.Serializable
import java.lang.Exception

fun interface SerializablePredicate<T> : Serializable/*, Predicate<T*/ {

    @Throws(Exception::class)
    fun test(t: T): Boolean
}