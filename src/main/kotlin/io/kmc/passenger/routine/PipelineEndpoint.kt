package io.kmc.passenger.routine

import io.kmc.passenger.routine.Passage.Align
import java.io.Serializable


open class PipelineEndpoint<T> : AbstractRoutinePipeline<T>  {


    var source: Passage.Entry<T>? = null

    companion object {
        @JvmStatic
        fun <T> create(entry: Passage.Entry<T>?): PipelineEndpoint<T> {
            return object : PipelineEndpoint<T>(entry) {

                var end: RoutinePipeline<T>

                init {
                    val head = this
                    end = object : PipelineEndpoint<T>(head) {
                        override fun _execute(target: T) {
                            // End of pipeline
                        }

                        override fun getLastRoutine(): RoutinePipeline<T> {
                            return previous!!
                        }
                    }
                }

                override fun getLastRoutine(): RoutinePipeline<T>? {
                    return end.getLastRoutine()
                }
            }
        }
    }

    constructor(entry: Passage.Entry<T>?) {
        source = entry
        root = this
    }

    constructor(root: AbstractRoutinePipeline<T>) : super(root)

    override fun integrate(passage: Passage<T>): Passage<T> {
        return source!!.integrate(passage!!)
    }

    override fun add(routine: RoutinePipeline<T>): RoutinePipeline<T> {
        val last: RoutinePipeline<T>? = getLastRoutine()
        last?.insert(routine)
        routine.changeRoot(root)
        return routine
    }

    override fun add(clazzName: String, methodName: String, vararg args: Serializable): ReflectRoutine<T> {
        val last: RoutinePipeline<T>? = getLastRoutine()
        return if (this === last) {
            super.add(clazzName, methodName, *args)
        } else last!!.add(clazzName, methodName, *args)
    }

    // @Override
    // public ReflectRoutine<T> add(String clazzName, String methodName, Integer targetIndex, Serializable... args ) {
    //     var last = getLastRoutine();
    //     if (this == last) {
    //         return super.add(clazzName, methodName, targetIndex, args);
    //     }
    //     return last.add(clazzName, methodName, targetIndex, args);
    // }

    // @Override
    // public ReflectRoutine<T> add(String clazzName, String methodName, Integer targetIndex, Serializable... args ) {
    //     var last = getLastRoutine();
    //     if (this == last) {
    //         return super.add(clazzName, methodName, targetIndex, args);
    //     }
    //     return last.add(clazzName, methodName, targetIndex, args);
    // }

    override fun <R> add(func: SerializableFunction<in T, out R?>): RoutinePipeline<T> {
        val last: RoutinePipeline<T>? = getLastRoutine()
        return if (this === last) {
            super.add(func)
        } else last?.add(func) ?: this
    }

    override fun add(func: SerializableConsumer<in T>): RoutinePipeline<T> {
        val last: RoutinePipeline<T>? = getLastRoutine()
        return if (this === last) {
            super.add(func!!)
        } else last?.add(func!!) ?: this
    }

    fun migrate(routine: RoutinePipeline<T>, align: Align) {
        if (source == null) return
        source?.add(routine, align)
    }

    override fun applyTo(target: T?): Passage<T> {
        source!!.applyTo(target)
        return source!!
    }

    /**
     * May casue issue when two routines are joined but triggering run() at second part of routine endpoint.
     * The routine pipeline will be executed starting from the second source even though they are already joined.
     */
    @Throws(Exception::class)
    override fun run() {
        source!!.run()
    }

    @Throws(Exception::class)
    override fun _execute(target: T) {
        // Do nothing
    }
}