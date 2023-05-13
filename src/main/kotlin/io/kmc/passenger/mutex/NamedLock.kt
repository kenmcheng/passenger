package io.kmc.passenger.mutex

import java.io.Serializable


class NamedLock : Serializable {

    private val storedKeys: MutableMap<Any, Long> = HashMap()

    @Synchronized
    fun tryLock(lockName: String): Boolean {
        if (storedKeys.containsKey(lockName)) {
            return false
        }
        storedKeys[lockName] = System.currentTimeMillis()
        return true
    }

    @Synchronized
    fun unlock(lockName: String) {
        storedKeys.remove(lockName)
    }

    fun getLockCount(): Int {
        return storedKeys.size
    }
}