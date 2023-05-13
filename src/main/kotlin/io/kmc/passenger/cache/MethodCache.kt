package io.kmc.passenger.cache

import io.kmc.passenger.mutex.NamedLock

import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.TimeoutException

object MethodCache {

    private val lock = NamedLock()

    private val theCache: MutableMap<Class<*>, MutableMap<String, MutableList<Method>>> = WeakHashMap()

    fun getCount(): Int {
        return theCache.size
    }

    @Synchronized
    @Throws(Exception::class)
    fun clear(force: Boolean = false) {
        val start = System.currentTimeMillis()
        while (!force && lock.getLockCount() > 0) {
            if (System.currentTimeMillis() - start > 5000) {
                throw TimeoutException("Failed to clear method cache. The cache is being updated by other threads!")
            }
            Thread.sleep(5)
            continue
        }
        theCache.clear()
    }

    fun getMethods(clazz: Class<*>): Map<String, MutableList<Method>>? {
        return theCache[clazz]
    }

    @Throws(Exception::class)
    fun getMethod(clazz: Class<*>, methodName: String, vararg argClazzes: Class<*>): Method {
        var m: Method? = null
        while (!theCache.containsKey(clazz)) {
            val lockName = clazz.name
            val isLockAcquired: Boolean = lock.tryLock(lockName)
            if (!isLockAcquired) {
                Thread.sleep(10)
                continue
            }
            try {
                val methods: Array<Method> = clazz.methods
//                val methodMap: MutableMap<String, MutableList<Method>> = Arrays.stream(methods)
//                        .collect(Collectors.groupingBy(Method::getName))
                val methodMap: MutableMap<String, MutableList<Method>> = methods
                        .groupByTo(mutableMapOf(), { it.name },{ it })

                theCache[clazz] = methodMap
            } finally {
                lock.unlock(lockName)
            }
        }
        val methods: List<Method?>? = theCache[clazz]!![methodName]
        if (methods != null) {
            for (method in methods) {
                if (arrayEqual(argClazzes, method!!.parameterTypes)) {
                    m = method
                    break
                }
            }
        }

        if (m == null) {
            m = clazz.getMethod(methodName, *argClazzes)
            val lockName = clazz.name + "-" + methodName
            val isLockAcquired: Boolean = lock.tryLock(lockName)
            if (isLockAcquired) {
                try {
                    if (theCache[clazz]!!.containsKey(methodName)) {
                        theCache[clazz]!![methodName]!!.add(m)
                    } else {
                        theCache[clazz]!!.put(methodName, Arrays.asList<Method?>(m))
                    }
                } finally {
                    lock.unlock(lockName)
                }
            }
        }
        return m!!
    }

    private fun arrayEqual(a1: Array<*>?, a2: Array<*>?): Boolean {
        if (a1 == null) {
            return a2 == null || a2.size == 0
        }
        if (a2 == null) {
            return a1.size == 0
        }
        if (a1.size != a2.size) {
            return false
        }
        for (i in a1.indices) {
            if (a1[i] !== a2[i]) {
                return false
            }
        }
        return true
    }
}