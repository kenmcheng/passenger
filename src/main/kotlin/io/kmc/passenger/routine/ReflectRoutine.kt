package io.kmc.passenger.routine

import io.kmc.passenger.ENABLE_METHOD_CACHE
import io.kmc.passenger.MAX_ATTEMPT_FETCH_METHOD
import io.kmc.passenger.cache.MethodCache
import java.io.Serializable
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class ReflectRoutine<T> (
        previousRoutine: AbstractRoutinePipeline<T>,
        val clazzName: String,
        val methodName: String = "",
        val targetIndex: Int = -1,
        vararg args: Serializable
) : AbstractRoutinePipeline<T> (previousRoutine), Reflection {

    private var args: Array<Any> = arrayOf(*args)

    @Throws(Exception::class)
    override fun run() {
        root?.run()
    }

    @Throws(Exception::class)
    override fun _execute(target: T) {
        val clazz = Class.forName(clazzName)
        if (clazz.isInstance(target)) {
            val m: Method = findMethod(clazz, methodName, *getClassArrayOfArgs(*args))
            m.invoke(target, *args)
        } else {
            val nArgs = insertArgArray(target)
            val argsClazzes = getClassArrayOfArgs(*nArgs)
            /*
             * If the method with the type of target cannot be found,
             * we attempt to fetch method that takes target's superclass as part of its arguments.
             */
            var m: Method? = null
            var e: Exception? = null
            val maxRetry = if (targetIndex < 0) 1 else MAX_ATTEMPT_FETCH_METHOD
            for (cnt in 0 until maxRetry) {
                try {
                    m = findMethod(clazz, methodName, *argsClazzes)
                    break
                } catch (ex: NoSuchMethodException) {
                    if (e == null) {
                        e = ex
                    }
                    if (targetIndex >= 0 && targetIndex < argsClazzes.size) {
                        argsClazzes[targetIndex] = argsClazzes[targetIndex]!!.superclass
                        if (argsClazzes[targetIndex] == null) break
                    }
                }
            }
            if (m == null) {
                throw e!!
            }
            if (Modifier.isStatic(m.getModifiers())) {
                m.invoke(null, *nArgs)
            } else {
                val inst = clazz.getDeclaredConstructor().newInstance()
                m.invoke(inst, *nArgs)
            }
        }
    }

    @Throws(Exception::class)
    private fun findMethod(clazz: Class<*>, methodName: String, vararg argsClazzes: Class<*>): Method {
        return if (ENABLE_METHOD_CACHE) {
            MethodCache.getMethod(clazz, methodName, *argsClazzes)
        } else clazz.getMethod(methodName, *argsClazzes)
    }

    private fun insertArgArray(target: T): Array<Any> {
        if (targetIndex >= 0 && targetIndex < args.size) {
            args[targetIndex] = target as Any
        }
        return args
    }

    private fun getClassArrayOfArgs(vararg nargs: Any): Array<Class<*>> {
        val sz = nargs.size
        val clazzes = arrayOfNulls<Class<*>>(sz) as Array<Class<*>>
        for (i in 0 until sz) {
            var argsClazz: Class<*> = nargs[i].javaClass
            while (argsClazz.isAnonymousClass) {
                argsClazz = argsClazz.superclass
            }
            clazzes[i] = argsClazz
        }
        return clazzes
    }

}